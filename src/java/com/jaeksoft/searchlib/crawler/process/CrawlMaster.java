/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.process;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabase;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManager;
import com.jaeksoft.searchlib.crawler.database.url.HostItem;
import com.jaeksoft.searchlib.crawler.database.url.UrlItem;
import com.jaeksoft.searchlib.util.DaemonThread;

public class CrawlMaster extends DaemonThread {

	private ArrayList<CrawlThread> crawlThreads;

	private ArrayList<HostItem> hostList;

	private Client client;
	private CrawlDatabase database;

	private CrawlStatistics sessionStats;
	private CrawlStatistics overallStats;

	private List<CrawlStatistics> statistics;

	private boolean extractionInProgress;

	public CrawlMaster(Client client) throws CrawlDatabaseException {
		super(true, 10);
		this.client = client;
		database = client.getCrawlDatabase();
		crawlThreads = null;
		hostList = new ArrayList<HostItem>();
		overallStats = new CrawlStatistics("Overall");
		sessionStats = new CrawlStatistics("Current session", overallStats);
		statistics = new ArrayList<CrawlStatistics>();
		statistics.add(overallStats);
		statistics.add(sessionStats);
		if (database.getPropertyManager().isCrawlEnabled())
			start();
	}

	public boolean start() {
		if (isRunning())
			return false;
		overallStats.reset();
		return super.start();
	}

	@Override
	public void runner() throws Exception {
		PropertyManager propertyManager = database.getPropertyManager();
		synchronized (this) {
			sessionStats.reset();
			hostList.clear();
			extractionInProgress = true;
			crawlThreads = new ArrayList<CrawlThread>();
			int threadNumber = propertyManager.getMaxThreadNumber();
			while (--threadNumber >= 0)
				crawlThreads.add(new CrawlThread(client, this));
		}
		extractHostList();
		waitForChild();
	}

	private void extractHostList() throws CrawlDatabaseException {
		PropertyManager propertyManager = database.getPropertyManager();
		long t = System.currentTimeMillis();
		database.getUrlManager().getHostToFetch(
				propertyManager.getFetchInterval(),
				propertyManager.getMaxUrlPerSession(), sessionStats, hostList);
		sessionStats.addExtractionTime(System.currentTimeMillis() - t);
		synchronized (this) {
			extractionInProgress = false;
		}
	}

	protected boolean isMoreHost() {
		synchronized (hostList) {
			if (hostList.size() == 0)
				return extractionInProgress;
			return true;
		}
	}

	protected List<UrlItem> getNextUrlList() throws CrawlDatabaseException {
		HostItem host = null;
		synchronized (hostList) {
			if (hostList.size() == 0)
				return null;
			host = hostList.get(0);
			hostList.remove(0);
		}

		PropertyManager propertyManager = database.getPropertyManager();
		long limit = propertyManager.getMaxUrlPerSession()
				- sessionStats.getFetchedCount();
		if (limit < 0)
			return null;
		int maxUrlPerHost = propertyManager.getMaxUrlPerHost();
		if (limit > maxUrlPerHost)
			limit = maxUrlPerHost;
		return database.getUrlManager().getUrlToFetch(host,
				propertyManager.getFetchInterval(), limit);
	}

	protected void deleteBadUrl(String sUrl) throws CrawlDatabaseException {
		database.getUrlManager().delete(sUrl);
	}

	public CrawlStatistics getSessionStatistics() {
		return sessionStats;
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	private boolean isChildRunning() {
		synchronized (this) {
			if (crawlThreads == null)
				return false;
			for (CrawlThread crawlThread : crawlThreads)
				if (crawlThread.isRunning())
					return true;
			return false;
		}
	}

	private void waitForChild() {
		while (isChildRunning())
			sleepSec(1);
	}

	@Override
	public void abort() {
		synchronized (this) {
			if (crawlThreads == null)
				return;
			for (CrawlThread crawlThread : crawlThreads)
				crawlThread.abort();
			super.abort();
		}
	}

	public List<CrawlThread> getCrawlThreads() {
		synchronized (this) {
			return crawlThreads;
		}
	}

}
