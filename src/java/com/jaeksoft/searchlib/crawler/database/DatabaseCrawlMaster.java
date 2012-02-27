/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.TreeMap;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class DatabaseCrawlMaster extends CrawlMasterAbstract {

	private TreeMap<DatabaseCrawl, DatabaseCrawlThread> threadMap;

	public DatabaseCrawlMaster(Config config) {
		super(config);
		threadMap = new TreeMap<DatabaseCrawl, DatabaseCrawlThread>();
	}

	public DatabaseCrawlThread execute(Client client,
			DatabaseCrawl databaseCrawl, boolean bWaitForCompletion,
			TaskLog taskLog) throws InterruptedException, SearchLibException {
		DatabaseCrawlThread databaseCrawlThread = null;
		synchronized (threadMap) {
			if (threadMap.containsKey(databaseCrawl)) {
				throw new SearchLibException("The job "
						+ databaseCrawl.getName() + " is already running");
			}
			databaseCrawlThread = new DatabaseCrawlThread(client, this,
					databaseCrawl, taskLog);
			threadMap.put(databaseCrawl, databaseCrawlThread);
		}
		databaseCrawl.setCrawlThread(databaseCrawlThread);
		add(databaseCrawlThread);
		if (bWaitForCompletion)
			databaseCrawlThread.waitForEnd(0);
		return databaseCrawlThread;
	}

	public boolean isDatabaseCrawlThread(DatabaseCrawl databaseCrawl) {
		synchronized (threadMap) {
			return threadMap.containsKey(databaseCrawl);
		}
	}

	public DatabaseCrawlThread getDatabaseCrawlThread(
			DatabaseCrawl databaseCrawl) {
		synchronized (threadMap) {
			return threadMap.get(databaseCrawl);
		}
	}

	@Override
	public String getStatusInfo() {
		synchronized (threadMap) {
			int l = threadMap.size();
			switch (l) {
			case 0:
				return "Not running";
			case 1:
				return "1 process";
			default:
				return l + " processes";

			}
		}
	}

	@Override
	public void remove(ThreadAbstract thread) {
		super.remove(thread);
		synchronized (threadMap) {
			threadMap.remove(((DatabaseCrawlThread) thread).getDatabaseCrawl());
			if (threadMap.size() == 0) {
				Config config = getConfig();
				if (config instanceof Client) {
					try {
						((Client) config).reload();
					} catch (SearchLibException e) {
						setException(e);
					}
				}
			}
		}
	}

	@Override
	public void runner() throws Exception {
	}

	@Override
	public CrawlQueueAbstract getCrawlQueue() {
		return null;
	}

}
