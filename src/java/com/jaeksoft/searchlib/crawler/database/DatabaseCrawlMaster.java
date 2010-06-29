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

package com.jaeksoft.searchlib.crawler.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeMap;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;

public class DatabaseCrawlMaster extends CrawlMasterAbstract {

	private TreeMap<DatabaseCrawl, DatabaseCrawlThread> threadMap;

	public DatabaseCrawlMaster(Config config) {
		super(config);
		threadMap = new TreeMap<DatabaseCrawl, DatabaseCrawlThread>();
	}

	public DatabaseCrawlThread execute(Client client,
			DatabaseCrawl databaseCrawl, boolean bWaitForCompletion)
			throws InterruptedException, SearchLibException {
		DatabaseCrawlThread databaseCrawlThread = null;
		synchronized (threadMap) {
			if (threadMap.containsKey(databaseCrawl)) {
				throw new SearchLibException("The job "
						+ databaseCrawl.getName() + " is already running");
			}
			databaseCrawlThread = new DatabaseCrawlThread(client, this,
					databaseCrawl);
			threadMap.put(databaseCrawl, databaseCrawlThread);
		}
		databaseCrawl.setCrawlThread(databaseCrawlThread);
		add(databaseCrawlThread);
		if (bWaitForCompletion)
			databaseCrawlThread.waitForEnd();
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
	public void remove(CrawlThreadAbstract crawlThread) {
		super.remove(crawlThread);
		synchronized (threadMap) {
			threadMap.remove(((DatabaseCrawlThread) crawlThread)
					.getDatabaseCrawl());
			if (threadMap.size() == 0) {
				if (config instanceof Client) {
					try {
						((Client) config).reload(null);
					} catch (IOException e) {
						setError(e);
					} catch (URISyntaxException e) {
						setError(e);
					} catch (SearchLibException e) {
						setError(e);
					} catch (InstantiationException e) {
						setError(e);
					} catch (IllegalAccessException e) {
						setError(e);
					} catch (ClassNotFoundException e) {
						setError(e);
					} catch (HttpException e) {
						setError(e);
					}
				}
			}
		}
	}

	@Override
	public void runner() throws Exception {
	}

}
