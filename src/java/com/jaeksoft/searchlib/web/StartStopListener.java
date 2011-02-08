/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.TaskManager;

public class StartStopListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		Logging.logger.info("OSS SHUTDOWN");
		try {
			TaskManager.stop();
		} catch (SearchLibException e) {
			Logging.logger.error(e);
		}
		ClientCatalog.closeAll();
	}

	protected ClientFactory getClientFactory() {
		return new ClientFactory();
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		Logging.initLogger();
		Logging.logger.info("OSS IS STARTING");
		ClientFactory.setInstance(getClientFactory());
		try {
			TaskManager.start();
		} catch (SearchLibException e) {
			Logging.logger.error(e);
		}
		ClientCatalog.openAll();
	}
}
