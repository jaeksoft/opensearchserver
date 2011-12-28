/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.scheduler.TaskManager;

public class StartStopListener implements ServletContextListener {

	public static File OPENSEARCHSERVER_DATA_FILE = null;

	private static void initDataDir(ServletContext servletContext) {
		String single_data = System.getenv("OPENSEARCHSERVER_DATA");
		String multi_data = System.getenv("OPENSEARCHSERVER_MULTIDATA");
		if (multi_data != null)
			OPENSEARCHSERVER_DATA_FILE = new File(new File(multi_data),
					servletContext.getContextPath());
		else if (single_data != null)
			OPENSEARCHSERVER_DATA_FILE = new File(single_data);
		if (!OPENSEARCHSERVER_DATA_FILE.exists())
			OPENSEARCHSERVER_DATA_FILE.mkdir();
		System.out.println("OPENSEARCHSERVER_DATA_FILE IS: "
				+ OPENSEARCHSERVER_DATA_FILE);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		Logging.info("OSS SHUTDOWN");
		try {
			TaskManager.stop();
		} catch (SearchLibException e) {
			Logging.error(e);
		}
		ClientCatalog.closeAll();
	}

	protected ClientFactory getClientFactory() throws SearchLibException {
		return new ClientFactory();
	}

	private static transient Version version = null;

	public static final synchronized Version getVersion() {
		return version;
	}

	protected String getEdition() {
		return "community edition";
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {

		ServletContext servletContext = contextEvent.getServletContext();
		initDataDir(servletContext);

		Logging.initLogger();
		Logging.info("OSS IS STARTING");

		try {
			version = new Version(servletContext, getEdition());
		} catch (IOException e) {
			Logging.error(e);
		}

		try {
			ClientFactory.setInstance(getClientFactory());
			TaskManager.start();
		} catch (SearchLibException e) {
			Logging.error(e);
		}
		ClientCatalog.openAll();
	}

}
