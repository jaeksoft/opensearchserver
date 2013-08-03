/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.database;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class DatabaseCrawlProcessController extends CrawlerController {

	private transient boolean debug = false;

	public DatabaseCrawlProcessController() throws SearchLibException,
			NamingException {
		super();
	}

	@Override
	protected void reset() {
		debug = false;
	}

	@Override
	public DatabaseCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getDatabaseCrawlMaster();
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		DatabaseCrawlMaster crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getThreadsCount() > 0;
	}

	@Override
	@Command
	public void onTimer() throws SearchLibException {
		reload();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
