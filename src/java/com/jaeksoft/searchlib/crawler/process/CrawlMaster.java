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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabase;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManager;
import com.jaeksoft.searchlib.crawler.database.url.HostCountItem;
import com.jaeksoft.searchlib.crawler.database.url.UrlItem;
import com.jaeksoft.searchlib.util.DaemonThread;

public class CrawlMaster extends DaemonThread {

	private ArrayList<CrawlThread> crawlThreads;

	private ArrayList<HostCountItem> hostList;
	private Iterator<HostCountItem> hostIterator;

	private Client client;
	private CrawlDatabase database;

	public CrawlMaster(Client client) {
		super(true, 1000);
		this.client = client;
		database = client.getCrawlDatabase();
		crawlThreads = null;
		hostList = null;
		hostIterator = null;
		start();
	}

	@Override
	public void runner() throws Exception {
		PropertyManager propertyManager = database.getPropertyManager();
		if (!propertyManager.isCrawlEnabled()) {
			DaemonThread.sleep(1000);
			return;
		}
		hostList = (ArrayList<HostCountItem>) database.getUrlManager()
				.getHostToFetch(propertyManager.getFetchInterval(),
						propertyManager.getMaxUrlPerSession());
		if (hostList != null)
			hostIterator = hostList.iterator();
		synchronized (this) {
			crawlThreads = new ArrayList<CrawlThread>();
			int threadNumber = propertyManager.getMaxThreadNumber();
			while (--threadNumber >= 0)
				crawlThreads.add(new CrawlThread(client, this));
		}
		waitForChild();
	}

	protected List<UrlItem> getNextUrlList() throws SQLException {
		synchronized (this) {
			if (hostIterator == null)
				return null;
			if (!hostIterator.hasNext())
				return null;
			PropertyManager propertyManager = database.getPropertyManager();
			HostCountItem host = hostIterator.next();
			int limit = propertyManager.getMaxUrlPerSession()
					- getFetchedCount();
			if (limit < 0)
				return null;
			int maxUrlPerHost = propertyManager.getMaxUrlPerHost();
			if (limit > maxUrlPerHost)
				limit = maxUrlPerHost;
			return database.getUrlManager().getUrlToFetch(host,
					propertyManager.getFetchInterval(), limit);
		}
	}

	protected void deleteBadUrl(String sUrl) {
		try {
			database.getUrlManager().delete(sUrl);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getFetchedCount() {
		synchronized (this) {
			if (crawlThreads == null)
				return 0;
			int r = 0;
			for (CrawlThread crawlThread : crawlThreads)
				r += crawlThread.getFetchedCount();
			return r;
		}
	}

	public float getFetchRate() {
		synchronized (this) {
			if (crawlThreads == null)
				return 0;
			float rate = 0;
			for (CrawlThread crawlThread : crawlThreads)
				rate += crawlThread.getFetchRate();
			return rate;
		}
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
			sleep(500);
	}

	@Override
	public void abort() {
		synchronized (this) {
			for (CrawlThread crawlThread : crawlThreads)
				crawlThread.abort();
		}
	}

	public List<CrawlThread> getCrawlThreads() {
		synchronized (this) {
			return crawlThreads;
		}
	}

}
