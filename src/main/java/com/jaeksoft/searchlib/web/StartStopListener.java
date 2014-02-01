/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.ThreadUtils.WaitInterface;

public class StartStopListener implements ServletContextListener {

	public static File OPENSEARCHSERVER_DATA_FILE = null;

	private final static ReadWriteLock rwl = new ReadWriteLock();

	private static boolean active = false;

	public static String REALPATH_WEBINF_CLASSES = null;

	public static String REALPATH_WEBINF_LIB = null;

	private static void initDataDir(ServletContext servletContext) {
		String single_data = System.getenv("OPENSEARCHSERVER_DATA");
		String multi_data = System.getenv("OPENSEARCHSERVER_MULTIDATA");
		if (multi_data == null)
			multi_data = System.getenv("OPENSHIFT_DATA_DIR");
		if (multi_data != null) {
			String p = servletContext.getContextPath();
			if ("".equals(p) || "/".equals(p))
				p = "ROOT";
			OPENSEARCHSERVER_DATA_FILE = new File(new File(multi_data), p);
		} else if (single_data != null)
			OPENSEARCHSERVER_DATA_FILE = new File(single_data);
		else
			OPENSEARCHSERVER_DATA_FILE = new File(
					System.getProperty("user.home"), "opensearchserver_data");
		if (!OPENSEARCHSERVER_DATA_FILE.exists())
			OPENSEARCHSERVER_DATA_FILE.mkdir();
		System.out.println("OPENSEARCHSERVER_DATA_FILE IS: "
				+ OPENSEARCHSERVER_DATA_FILE);
	}

	private static void initDataDir(File data_directory) {
		if (OPENSEARCHSERVER_DATA_FILE != null)
			throw new RuntimeException("Data directory already set: "
					+ OPENSEARCHSERVER_DATA_FILE.getAbsolutePath());
		if (data_directory.exists()) {
			if (!data_directory.isDirectory())
				throw new RuntimeException(data_directory.getAbsolutePath()
						+ " is not a directory");
		}
		OPENSEARCHSERVER_DATA_FILE = data_directory;
		if (!OPENSEARCHSERVER_DATA_FILE.exists())
			OPENSEARCHSERVER_DATA_FILE.mkdir();
	}

	public static void setActive(boolean active) {
		rwl.w.lock();
		try {
			StartStopListener.active = active;
		} finally {
			rwl.w.unlock();
		}
	}

	public static boolean isShutdown() {
		rwl.r.lock();
		try {
			return StartStopListener.active == false;
		} finally {
			rwl.r.unlock();
		}
	}

	public static class ShutdownWaitInterface implements WaitInterface {

		@Override
		public boolean done() {
			return false;
		}

		@Override
		public boolean abort() {
			return isShutdown();
		}
	}

	public final static void shutdown() {
		setActive(false);
		Logging.info("OSS SHUTDOWN");
		try {
			TaskManager.getInstance().stop();
		} catch (SearchLibException e) {
			Logging.error(e);
		}
		ClientCatalog.closeAll();
		ErrorParserLogger.close();
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		shutdown();
	}

	private static transient Version version = null;

	public static final synchronized Version getVersion() {
		return version;
	}

	public static class ThreadedLoad implements Runnable {

		public ThreadedLoad() {
			new Thread(ClientCatalog.getThreadGroup(), this).start();
		}

		@Override
		public void run() {
			Logging.info("OSS starts loading index(es)");
			ClientCatalog.openAll();
			Logging.info("OSS ends loading index(es)");
		}

	}

	private static final void start() {
		setActive(true);

		Logging.initLogger();
		Logging.info("OSS IS STARTING ");

		ErrorParserLogger.init();

		Logging.info("Native library version: "
				+ OsseIndex.LIB.OSSCLib_GetVersionInfoText());

		try {
			ClientFactory.setInstance(new ClientFactory());
			TaskManager.getInstance().start();
		} catch (SearchLibException e) {
			Logging.error(e);
		}
	}

	public static void start(File data_directory) {
		initDataDir(data_directory);
		start();
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		ServletContext servletContext = contextEvent.getServletContext();
		REALPATH_WEBINF_CLASSES = servletContext
				.getRealPath("/WEB-INF/classes");
		REALPATH_WEBINF_LIB = servletContext.getRealPath("/WEB-INF/lib");
		initDataDir(servletContext);
		try {
			version = new Version(servletContext);
		} catch (IOException e) {
			Logging.error(e);
		}
		start();
		new ThreadedLoad();
	}
}
