/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.process;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.web.database.NamedItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class CrawlThread extends CrawlThreadAbstract {

	private Config config;
	private CrawlMaster crawlMaster;
	private UrlItem currentUrlItem;
	private CrawlStatistics currentStats;
	private long delayBetweenAccesses;
	private HttpDownloader httpDownloader;
	private HttpDownloader httpDownloaderRobotsTxt;
	private long nextTimeTarget;
	private List<UrlItem> urlList;
	private NamedItem host;

	protected CrawlThread(Config config, CrawlMaster crawlMaster,
			CrawlStatistics sessionStats, List<UrlItem> urlList, NamedItem host)
			throws SearchLibException {
		this.host = host;
		this.config = config;
		this.crawlMaster = crawlMaster;
		this.currentUrlItem = null;
		currentStats = new CrawlStatistics(sessionStats);
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		delayBetweenAccesses = propertyManager.getDelayBetweenAccesses()
				.getValue();
		nextTimeTarget = 0;
		this.urlList = urlList;
		httpDownloader = new HttpDownloader(propertyManager.getUserAgent()
				.getValue(), false);
		httpDownloaderRobotsTxt = new HttpDownloader(propertyManager
				.getUserAgent().getValue(), true);

	}

	private void sleepInterval() {
		long ms = nextTimeTarget - System.currentTimeMillis();
		if (ms < 0)
			return;
		sleepMs(ms);
	}

	@Override
	public void runner() throws Exception {

		WebPropertyManager propertyManager = config.getWebPropertyManager();
		boolean dryRun = propertyManager.getDryRun().getValue();

		currentStats.addListSize(urlList.size());

		Iterator<UrlItem> iterator = urlList.iterator();
		UrlCrawlQueue crawlQueue = crawlMaster.getCrawlQueue();

		while (iterator.hasNext()) {

			if (isAbort() || crawlMaster.isAbort())
				break;

			if (crawlMaster.urlLeft() < 0)
				break;

			currentUrlItem = iterator.next();

			Crawl crawl = crawl(dryRun);
			if (crawl != null) {
				if (!dryRun)
					crawlQueue.add(currentStats, crawl);
			} else {
				if (!dryRun)
					crawlQueue.delete(currentStats, currentUrlItem.getUrl());
			}

		}

		setStatus(CrawlStatus.INDEXATION);
		if (!dryRun)
			crawlMaster.getCrawlQueue().index(false);

	}

	private Crawl crawl(boolean dryRun) throws SearchLibException {

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		Crawl crawl = new Crawl(currentUrlItem, config, config
				.getParserSelector());

		try {
			// Check the url
			URL url = currentUrlItem.getURL();

			// Check if url is allowed by pattern list
			PatternManager patternManager = config.getPatternManager();
			if (url != null)
				if (patternManager.matchPattern(url) == null)
					url = null;
			if (url == null)
				return null;

			// Fetch started
			currentStats.incFetchedCount();
			if (dryRun)
				return crawl;

			sleepInterval();
			setStatus(CrawlStatus.CRAWL);
			if (crawl.checkRobotTxtAllow(httpDownloaderRobotsTxt))
				crawl.download(httpDownloader);
			nextTimeTarget = System.currentTimeMillis() + delayBetweenAccesses
					* 1000;

			if (currentUrlItem.getFetchStatus() == FetchStatus.FETCHED
					&& currentUrlItem.getParserStatus() == ParserStatus.PARSED
					&& currentUrlItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
				currentUrlItem.setIndexStatus(IndexStatus.INDEXED);
				currentStats.incParsedCount();
			} else
				currentStats.incIgnoredCount();

		} catch (MalformedURLException e) {
			crawl.setError(e.getMessage());
			currentUrlItem.setFetchStatus(FetchStatus.URL_ERROR);
		}

		return crawl;
	}

	@Override
	public void abort() {
		super.abort();
	}

	public boolean getCrawlTimeOutExhausted(int seconds) {
		synchronized (this) {
			if (getStatus() != CrawlStatus.CRAWL)
				return false;
			return getStatusTimeElapsed() > seconds;
		}
	}

	public UrlItem getCurrentUrlItem() {
		synchronized (this) {
			return currentUrlItem;
		}
	}

	public NamedItem getHost() {
		synchronized (this) {
			return host;
		}
	}

	public void setCurrentUrlItem(UrlItem urlItem) {
		synchronized (this) {
			currentUrlItem = urlItem;
		}
	}

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
	}

	@Override
	public void complete() {
		crawlMaster.remove(this);
		httpDownloader.release();
		httpDownloaderRobotsTxt.release();
		synchronized (crawlMaster) {
			crawlMaster.notify();
		}

	}

	public String getDebugInfo() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(getThreadStatus());
			sb.append(' ');
			if (currentUrlItem != null)
				sb.append(currentUrlItem.getUrl());
			return sb.toString();
		}
	}

}