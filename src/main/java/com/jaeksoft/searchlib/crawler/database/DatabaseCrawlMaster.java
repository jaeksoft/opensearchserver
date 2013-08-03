/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class DatabaseCrawlMaster extends
		CrawlMasterAbstract<DatabaseCrawlMaster, DatabaseCrawlThread> {

	public DatabaseCrawlMaster(Config config) {
		super(config);
	}

	@Override
	public DatabaseCrawlThread getNewThread(Client client,
			ThreadItem<?, DatabaseCrawlThread> databaseCrawl, TaskLog taskLog) {
		if (databaseCrawl instanceof DatabaseCrawlSql)
			return new DatabaseCrawlSqlThread(client, this,
					(DatabaseCrawlSql) databaseCrawl, taskLog);
		return null;
	}

	@Override
	protected DatabaseCrawlThread[] getNewArray(int size) {
		return new DatabaseCrawlThread[size];
	}
}
