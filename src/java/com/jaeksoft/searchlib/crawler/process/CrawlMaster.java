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
import com.jaeksoft.searchlib.crawler.urldb.HostCountItem;
import com.jaeksoft.searchlib.crawler.urldb.UrlDb;
import com.jaeksoft.searchlib.crawler.urldb.UrlItem;

public class CrawlMaster {

	private String userAgent;
	private int maxUrl;
	private long delayBetweenAccesses;
	private int maxUrlPerHost;
	private int fetchInterval;
	private ArrayList<CrawlThread> crawlThreads;

	private ArrayList<HostCountItem> hostList;
	private Iterator<HostCountItem> hostIterator;

	private UrlDb urlDb;

	private boolean abort;

	private Client client;

	public CrawlMaster(Client client) {
		this.client = client;
		userAgent = null;
		maxUrl = 0;
		delayBetweenAccesses = 0;
		maxUrlPerHost = 0;
		fetchInterval = 0;
		crawlThreads = null;
		hostList = null;
		hostIterator = null;
		urlDb = null;
		abort = false;
	}

	public void start(String userAgent, int maxUrl, int threadNumber,
			int delayBetweenAccesses, int maxUrlPerHost, int fetchInterval)
			throws SQLException {
		synchronized (this) {
			if (isRunning())
				return;
			this.abort = false;
			this.userAgent = userAgent;
			this.maxUrl = maxUrl;
			this.delayBetweenAccesses = delayBetweenAccesses;
			this.maxUrlPerHost = maxUrlPerHost;
			this.fetchInterval = fetchInterval;

			this.urlDb = new UrlDb(client);
			hostList = (ArrayList<HostCountItem>) urlDb.getHostToFetch(
					fetchInterval, maxUrl);
			if (hostList != null)
				hostIterator = hostList.iterator();
			crawlThreads = new ArrayList<CrawlThread>();
			while (--threadNumber >= 0)
				crawlThreads.add(new CrawlThread(client, this));
		}
	}

	protected ArrayList<UrlItem> getNextUrlList() throws SQLException {
		synchronized (this) {
			if (hostIterator == null)
				return null;
			if (!hostIterator.hasNext())
				return null;
			HostCountItem host = hostIterator.next();
			int limit = maxUrl - getFetchedCount();
			if (limit < 0)
				return null;
			if (limit > maxUrlPerHost)
				limit = maxUrlPerHost;
			return urlDb.getUrlToFetch(host, fetchInterval, limit);
		}
	}

	protected void deleteBadUrl(String sUrl) {
		try {
			urlDb.delete(sUrl);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String getUserAgent() {
		return this.userAgent;
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

	public boolean isRunning() {
		synchronized (this) {
			if (crawlThreads == null)
				return false;
			for (CrawlThread crawlThread : crawlThreads)
				if (crawlThread.isRunning())
					return true;
			return false;
		}
	}

	protected void sleepInterval() {
		if (delayBetweenAccesses == 0)
			return;
		try {
			Thread.sleep(delayBetweenAccesses * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void abort() {
		synchronized (this) {
			abort = true;
			for (CrawlThread crawlThread : crawlThreads)
				crawlThread.abort();
		}
	}

	public boolean getAbort() {
		synchronized (this) {
			if (!isRunning())
				abort = false;
			return abort;
		}
	}

	public List<CrawlThread> getCrawlThreads() {
		synchronized (this) {
			return crawlThreads;
		}
	}

}
