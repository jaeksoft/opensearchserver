/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.database;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class DatabaseCrawlProcessController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1902174922255951773L;

	private boolean debug;

	public DatabaseCrawlProcessController() throws SearchLibException,
			NamingException {
		super();
		debug = false;
	}

	public DatabaseCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getDatabaseCrawlMaster();
	}

	public boolean isRefresh() throws SearchLibException {
		DatabaseCrawlMaster crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getCrawlThreadsSize() > 0;
	}

	public void onTimer() {
		reloadPage();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public void reset() {
	}

}
