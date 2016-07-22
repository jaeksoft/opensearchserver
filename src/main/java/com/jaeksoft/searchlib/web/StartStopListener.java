/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.util.ThreadUtils.WaitInterface;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class StartStopListener implements ServletContextListener {

	public static File OPENSEARCHSERVER_DATA_FILE = null;

	private static final AtomicBoolean active = new AtomicBoolean(false);

	private static final AtomicBoolean started = new AtomicBoolean(false);

	public static String REALPATH_WEBINF_CLASSES = null;

	public static String REALPATH_WEBINF_LIB = null;

	private static void initDataDir(ServletContext servletContext) {
		String single_data = System.getenv("OPENSEARCHSERVER_DATA");
		if (single_data == null)
			single_data = System.getProperty("OPENSEARCHSERVER_DATA");
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
			OPENSEARCHSERVER_DATA_FILE = new File(System.getProperty("user.home"), "opensearchserver_data");
		if (!OPENSEARCHSERVER_DATA_FILE.exists())
			OPENSEARCHSERVER_DATA_FILE.mkdir();
		System.out.println("OPENSEARCHSERVER_DATA_FILE IS: " + OPENSEARCHSERVER_DATA_FILE);
	}

	private static void initDataDir(File data_directory) {
		if (OPENSEARCHSERVER_DATA_FILE != null)
			throw new RuntimeException("Data directory already set: " + OPENSEARCHSERVER_DATA_FILE.getAbsolutePath());
		if (data_directory.exists()) {
			if (!data_directory.isDirectory())
				throw new RuntimeException(data_directory.getAbsolutePath() + " is not a directory");
		}
		OPENSEARCHSERVER_DATA_FILE = data_directory;
		if (!OPENSEARCHSERVER_DATA_FILE.exists())
			OPENSEARCHSERVER_DATA_FILE.mkdir();
	}

	public static boolean isShutdown() {
		return !active.get();
	}

	public static boolean isStarted() {
		return started.get();
	}

	public static class StartedWaitInterface implements WaitInterface {

		@Override
		public boolean done() {
			return started.get();
		}

		@Override
		public boolean abort() {
			return false;
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
		active.set(false);
		started.set(false);
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
			started.set(true);
		}

	}

	private static final void start() {
		active.set(true);

		Logging.initLogger();
		Logging.info("OSS IS STARTING ");

		ErrorParserLogger.init();

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
		REALPATH_WEBINF_CLASSES = servletContext.getRealPath("/WEB-INF/classes");
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
