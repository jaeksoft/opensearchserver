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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabase;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.database.url.FetchStatus;
import com.jaeksoft.searchlib.crawler.database.url.IndexStatus;
import com.jaeksoft.searchlib.crawler.database.url.ParserStatus;
import com.jaeksoft.searchlib.crawler.database.url.UrlItem;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.util.DaemonThread;

public class CrawlThread extends DaemonThread {

	final private static Logger logger = Logger.getLogger(CrawlThread.class);

	private Client client;
	private CrawlDatabase database;
	private CrawlMaster crawlMaster;
	private long resetTime;
	private float fetchRate;
	private long fetchedCount;
	private long deletedCount;
	private long indexedCount;
	private long ignoredCount;
	private UrlItem currentUrlItem;
	private List<UrlItem> currentUrlList;

	protected CrawlThread(Client client, CrawlMaster crawlMaster) {
		super(false, 0);
		this.client = client;
		this.database = client.getCrawlDatabase();
		this.crawlMaster = crawlMaster;
		this.currentUrlItem = null;
		this.currentUrlList = null;
		resetStatistics();
		start();
	}

	private void resetStatistics() {
		synchronized (this) {
			fetchedCount = 0;
			deletedCount = 0;
			indexedCount = 0;
			ignoredCount = 0;
			fetchRate = 0;
			resetTime = System.currentTimeMillis();
		}
	}

	private void sleepInterval() throws CrawlDatabaseException {
		int delayBetweenAccesses = 0;
		synchronized (this) {
			delayBetweenAccesses = database.getPropertyManager()
					.getDelayBetweenAccesses();
			if (delayBetweenAccesses == 0)
				return;
		}
		try {
			Thread.sleep(delayBetweenAccesses * 1000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void runner() throws Exception {
		logger.info("CrawlThread " + this + " starts");
		try {
			String userAgent = database.getPropertyManager().getUserAgent();
			List<UrlItem> urlList = null;
			loop: while ((urlList = crawlMaster.getNextUrlList()) != null) {
				resetStatistics();
				setCurrentUrlList(urlList);
				for (UrlItem urlItem : urlList) {
					if (getAbort())
						break loop;
					crawl(userAgent, urlItem);
					sleepInterval();
				}
				client.reload();
			}
		} catch (CrawlDatabaseException e) {
			setError(e);
		} catch (IOException e) {
			setError(e);
		}
		logger.info("CrawlThread " + this + " ends");
	}

	private void setCurrentUrlList(List<UrlItem> urlList) {
		synchronized (this) {
			currentUrlList = urlList;
		}
	}

	private void crawl(String userAgent, UrlItem urlItem) {
		synchronized (this) {
			currentUrlItem = urlItem;
		}
		URL url;
		try {
			url = urlItem.getURL();
		} catch (MalformedURLException e) {
			logger.warn(urlItem.getUrl() + " " + e.getMessage());
			return;
		}
		try {
			PatternUrlManager patternManager = database.getPatternUrlManager();
			if (url != null)
				if (patternManager.findPatternUrl(url) == null)
					url = null;
			if (url == null) {
				crawlMaster.deleteBadUrl(urlItem.getUrl());
				incDeletedCount();
				return;
			}
			incFetchedCount();
			Crawl crawl = new Crawl(client, userAgent, urlItem);
			if (urlItem.getFetchStatus() == FetchStatus.FETCHED
					&& urlItem.getParserStatus() == ParserStatus.PARSED
					&& urlItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
				client.updateDocument(crawl.getDocument());
				urlItem.setIndexStatus(IndexStatus.INDEXED);
				incIndexedCount();
			} else
				incIgnoredCount();
			database.getUrlManager().update(crawl);
		} catch (Crawl.CrawlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			abort();
			setError(e);
		} catch (CrawlDatabaseException e) {
			abort();
			setError(e);
		} catch (NoSuchAlgorithmException e) {
			abort();
			setError(e);
		}
	}

	private void incDeletedCount() {
		synchronized (this) {
			deletedCount++;
		}
	}

	private void incFetchedCount() {
		synchronized (this) {
			fetchedCount++;
			fetchRate = (float) fetchedCount
					/ ((float) (System.currentTimeMillis() - resetTime) / 60000);
		}
	}

	private void incIndexedCount() {
		synchronized (this) {
			indexedCount++;
		}
	}

	private void incIgnoredCount() {
		synchronized (this) {
			ignoredCount++;
		}
	}

	public UrlItem getCurrentUrlItem() {
		synchronized (this) {
			return currentUrlItem;
		}
	}

	public long getFetchedCount() {
		synchronized (this) {
			return fetchedCount;
		}
	}

	public double getFetchRate() {
		synchronized (this) {
			return fetchRate;
		}

	}

	public long getDeletedCount() {
		synchronized (this) {
			return deletedCount;
		}
	}

	public long getIndexedCount() {
		synchronized (this) {
			return indexedCount;
		}
	}

	public long getIgnoredCount() {
		synchronized (this) {
			return ignoredCount;
		}
	}

	public long getUrlListSize() {
		synchronized (this) {
			if (currentUrlList == null)
				return 0;
			return currentUrlList.size();
		}
	}

}