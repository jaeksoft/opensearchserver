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
	private UrlItem currentUrlItem;
	private CrawlStatistics currentStats;
	private CrawlStatistics sessionStats;

	protected CrawlThread(Client client, CrawlMaster crawlMaster) {
		super(false, 0);
		this.client = client;
		this.database = client.getCrawlDatabase();
		this.crawlMaster = crawlMaster;
		this.currentUrlItem = null;
		sessionStats = new CrawlStatistics("Session", crawlMaster
				.getSessionStatistics());
		currentStats = new CrawlStatistics("Current", sessionStats);
		start();
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
		sessionStats.reset();
		try {
			String userAgent = database.getPropertyManager().getUserAgent();
			List<UrlItem> urlList = null;
			loop: while ((urlList = crawlMaster.getNextUrlList()) != null) {
				currentStats.reset();
				currentStats.addUrlCount(urlList.size());
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
				currentStats.incDeletedCount();
				return;
			}
			currentStats.incFetchedCount();
			Crawl crawl = new Crawl(client, userAgent, urlItem);
			if (urlItem.getFetchStatus() == FetchStatus.FETCHED
					&& urlItem.getParserStatus() == ParserStatus.PARSED
					&& urlItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
				client.updateDocument(crawl.getDocument());
				urlItem.setIndexStatus(IndexStatus.INDEXED);
				currentStats.incIndexedCount();
			} else
				currentStats.incIgnoredCount();
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

	public UrlItem getCurrentUrlItem() {
		synchronized (this) {
			return currentUrlItem;
		}
	}

	public CrawlStatistics getSessionStatistics() {
		return sessionStats;
	}

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
	}

}