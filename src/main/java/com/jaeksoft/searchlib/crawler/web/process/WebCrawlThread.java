/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.process;

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
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.pattern.PatternListMatcher;
import com.jaeksoft.searchlib.crawler.web.script.WebScriptItem;
import com.jaeksoft.searchlib.crawler.web.script.WebScriptManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class WebCrawlThread extends CrawlThreadAbstract<WebCrawlThread, WebCrawlMaster> {

	private UrlItem currentUrlItem;
	private long delayBetweenAccesses;
	private HttpDownloader httpDownloader;
	private HttpDownloader httpDownloaderRobotsTxt;
	private long nextTimeTarget;
	private HostUrlList hostUrlList;
	private Crawl currentCrawl;
	private PatternListMatcher exclusionMatcher;
	private PatternListMatcher inclusionMatcher;
	private UrlCrawlQueue crawlQueue;
	private final WebScriptManager webScriptManager;

	protected WebCrawlThread(Config config, WebCrawlMaster crawlMaster, CrawlStatistics sessionStats,
			HostUrlList hostUrlList) throws SearchLibException, IOException {
		super(config, crawlMaster, null, null);
		this.crawlQueue = (UrlCrawlQueue) crawlMaster.getCrawlQueue();
		this.currentUrlItem = null;
		this.currentCrawl = null;
		currentStats = new CrawlStatistics(sessionStats);
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		delayBetweenAccesses = propertyManager.getDelayBetweenAccesses().getValue();
		nextTimeTarget = 0;
		this.hostUrlList = hostUrlList;
		httpDownloader = crawlMaster.getNewHttpDownloader(false);
		httpDownloaderRobotsTxt =
				new HttpDownloader(propertyManager.getUserAgent().getValue(), true, propertyManager.getProxyHandler(),
						propertyManager.getConnectionTimeOut().getValue() * 1000);
		exclusionMatcher = propertyManager.getExclusionEnabled().getValue() ?
				config.getExclusionPatternManager().getPatternListMatcher() :
				null;
		inclusionMatcher = propertyManager.getInclusionEnabled().getValue() ?
				config.getInclusionPatternManager().getPatternListMatcher() :
				null;
		webScriptManager = config.getWebScriptManager();
	}

	private void sleepInterval() throws InterruptedException {
		long ms = nextTimeTarget - System.currentTimeMillis();
		if (ms < 0)
			return;
		sleepMs(ms);
	}

	@Override
	public void runner() throws Exception {

		List<UrlItem> urlList = hostUrlList.getUrlList();
		currentStats.addListSize(urlList.size());

		Iterator<UrlItem> iterator = urlList.iterator();
		WebCrawlMaster crawlMaster = (WebCrawlMaster) getThreadMaster();

		List<WebScriptItem> scriptList = webScriptManager.getItems("http://" + hostUrlList.getNamedItem().getName());
		if (scriptList != null)
			for (WebScriptItem scriptItem : scriptList)
				scriptItem.exec(httpDownloader);

		while (iterator.hasNext()) {

			ListType listType = hostUrlList.getListType();
			if (listType != ListType.MANUAL) {
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

		urlList.clear();
	}

	private Crawl crawl() throws SearchLibException, InterruptedException, IOException {

		final Config config = getConfig();

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		final Crawl crawl = ((WebCrawlMaster) getThreadMaster()).getNewCrawl(this);

		try {

			// Check the url
			URL url = currentUrlItem.getURL();

			// Check if url is allowed by pattern list
			if (url != null)
				if (inclusionMatcher != null && !inclusionMatcher.matchPattern(url, null)) {
					currentUrlItem.setFetchStatus(FetchStatus.NOT_IN_INCLUSION_LIST);
					url = null;
				}
			if (url != null)
				if (exclusionMatcher != null && exclusionMatcher.matchPattern(url, null)) {
					currentUrlItem.setFetchStatus(FetchStatus.BLOCKED_BY_EXCLUSION_LIST);
					url = null;
				}

			if (url == null)
				return null;

			// Fetch started
			currentStats.incFetchedCount();

			sleepInterval();
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
				currentUrlItem.setIndexStatus(IndexStatus.TO_INDEX);
				currentStats.incParsedCount();
				config.getScreenshotManager().capture(url, crawl.getCredentialItem(), true, 120);
			} else
				currentStats.incIgnoredCount();

		} catch (MalformedURLException e) {
			crawl.setError(e.getMessage());
			currentUrlItem.setFetchStatus(FetchStatus.URL_ERROR);
		} catch (URISyntaxException e) {
			crawl.setError(e.getMessage());
			currentUrlItem.setFetchStatus(FetchStatus.URL_ERROR);
		} catch (ClassNotFoundException e) {
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

	@Override
	public void release() {
		if (httpDownloader != null)
			httpDownloader.release();
		if (httpDownloader != null)
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