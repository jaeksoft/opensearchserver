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

package com.jaeksoft.searchlib.scheduler.task;

import java.util.Properties;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawl;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;

public class TaskDatabaseCrawlerRun extends TaskAbstract {

	private String[] propsName = { "crawl name" };

	@Override
	public String getName() {
		return "Database crawler - run";
	}

	@Override
	public String[] getPropertyList() {
		return propsName;
	}

	@Override
	public String[] getPropertyValues(Client client, String property)
			throws SearchLibException {
		DatabaseCrawlList crawlList = client.getDatabaseCrawlList();
		DatabaseCrawl[] crawls = crawlList.getArray();
		if (crawls == null)
			return null;
		String[] values = new String[crawls.length];
		for (int i = 0; i < values.length; i++)
			values[i] = crawls[i].getName();
		return values;
	}

	@Override
	public void execute(Client client, Properties properties)
			throws SearchLibException {
		DatabaseCrawlMaster crawlMaster = client.getDatabaseCrawlMaster();
		DatabaseCrawlList crawlList = client.getDatabaseCrawlList();
		String crawlName = properties.getProperty(propsName[0]);
		if (crawlName == null)
			return;
		DatabaseCrawl crawl = crawlList.get(crawlName);
		if (crawl == null)
			return;
		try {
			crawlMaster.execute(client, crawl, true);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
	}
}
