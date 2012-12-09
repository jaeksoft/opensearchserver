/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class WebCrawlThread extends CrawlThreadAbstract {

	private UrlItem currentUrlItem;
	private long delayBetweenAccesses;
	private HttpDownloader httpDownloader;
	private HttpDownloader httpDownloaderRobotsTxt;
	private long nextTimeTarget;
	private HostUrlList hostUrlList;
	private Crawl currentCrawl;
	private boolean exclusionEnabled;
	private boolean inclusionEnabled;
	private UrlCrawlQueue crawlQueue;

	protected WebCrawlThread(Config config, WebCrawlMaster crawlMaster,
			CrawlStatistics sessionStats, HostUrlList hostUrlList)
			throws SearchLibException {
		super(config, crawlMaster);
		this.crawlQueue = (UrlCrawlQueue) crawlMaster.getCrawlQueue();
		this.currentUrlItem = null;
		this.currentCrawl = null;
		currentStats = new CrawlStatistics(sessionStats);
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		delayBetweenAccesses = propertyManager.getDelayBetweenAccesses()
				.getValue();
		nextTimeTarget = 0;
		this.hostUrlList = hostUrlList;
		httpDownloader = crawlMaster.getNewHttpDownloader();
		httpDownloaderRobotsTxt = new HttpDownloader(propertyManager
				.getUserAgent().getValue(), true,
				propertyManager.getProxyHandler());
		exclusionEnabled = propertyManager.getExclusionEnabled().getValue();
		inclusionEnabled = propertyManager.getInclusionEnabled().getValue();

	}

	final protected WebCrawlMaster getWebCrawlMaster() {
		return (WebCrawlMaster) getCrawlMasterAbstract();
	}

	private void sleepInterval(long max) {
		long ms = nextTimeTarget - System.currentTimeMillis();
		if (ms < 0)
			return;
		if (ms > max)
			ms = max;
		sleepMs(ms);
	}

	@Override
	public void runner() throws Exception {

		List<UrlItem> urlList = hostUrlList.getUrlList();
		currentStats.addListSize(urlList.size());

		Iterator<UrlItem> iterator = urlList.iterator();
		WebCrawlMaster crawlMaster = (WebCrawlMaster) getThreadMaster();

		while (iterator.hasNext()) {

			ListType listType = hostUrlList.getListType();
			if (listType == ListType.NEW_URL || listType == ListType.OLD_URL) {
				if (crawlMaster.isAborted())
					break;
				if (crawlMaster.urlLeft() < 0)
					break;
			}

			currentUrlItem = iterator.next();

			currentCrawl = crawl();
			if (currentCrawl != null)
				crawlQueue.add(currentStats, currentCrawl);
			else
				crawlQueue.delete(currentStats, currentUrlItem.getUrl());

			if (isAborted())
				break;
		}

		setStatus(CrawlStatus.INDEXATION);
		crawlQueue.index(!crawlMaster.isRunning());

	}

	private Crawl crawl() throws SearchLibException {

		Config config = getConfig();

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		Crawl crawl = getWebCrawlMaster().getNewCrawl(this);

		try {
			// Check the url
			URL url = currentUrlItem.getURL();

			// Check if url is allowed by pattern list
			PatternManager inclusionManager = config
					.getInclusionPatternManager();
			PatternManager exclusionManager = config
					.getExclusionPatternManager();
			if (url != null)
				if (inclusionEnabled && !inclusionManager.matchPattern(url)) {
					currentUrlItem
							.setFetchStatus(FetchStatus.NOT_IN_INCLUSION_LIST);
					url = null;
				}
			if (url != null)
				if (exclusionEnabled && exclusionManager.matchPattern(url)) {
					currentUrlItem
							.setFetchStatus(FetchStatus.BLOCKED_BY_EXCLUSION_LIST);
					url = null;
				}

			if (url == null)
				return null;

			// Fetch started
			currentStats.incFetchedCount();

			sleepInterval(60000);
			setStatus(CrawlStatus.CRAWL);
			// NextTimeTarget is immediate by default
			nextTimeTarget = System.currentTimeMillis();
			if (crawl.checkRobotTxtAllow(httpDownloaderRobotsTxt)) {
				DownloadItem downloadItem = crawl.download(httpDownloader);
				// If we really crawled the content we honor the pause
				if (downloadItem == null || !downloadItem.isFromCache())
					nextTimeTarget += +delayBetweenAccesses * 1000;
				else
					currentStats.incFromCacheCount();
			}

			if (currentUrlItem.getFetchStatus() == FetchStatus.FETCHED
					&& currentUrlItem.getParserStatus() == ParserStatus.PARSED
					&& currentUrlItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
				currentUrlItem.setIndexStatus(IndexStatus.INDEXED);
				currentStats.incParsedCount();
				config.getScreenshotManager().capture(url,
						crawl.getCredentialItem(), true, 120);
			} else
				currentStats.incIgnoredCount();

		} catch (MalformedURLException e) {
			crawl.setError(e.getMessage());
			currentUrlItem.setFetchStatus(FetchStatus.URL_ERROR);
		}

		return crawl;
	}

	public UrlItem getCurrentUrlItem() {
		synchronized (this) {
			return currentUrlItem;
		}
	}

	public Crawl getCurrentCrawl() {
		synchronized (this) {
			return currentCrawl;
		}
	}

	public HostUrlList getHostUrlList() {
		synchronized (this) {
			return hostUrlList;
		}
	}

	public void setCurrentUrlItem(UrlItem urlItem) {
		synchronized (this) {
			currentUrlItem = urlItem;
		}
	}

	@Override
	public void release() {
		httpDownloader.release();
		httpDownloaderRobotsTxt.release();
		super.release();
	}

	@Override
	protected String getCurrentInfo() {
		if (currentUrlItem == null)
			return "";
		return currentUrlItem.getUrl();
	}

}