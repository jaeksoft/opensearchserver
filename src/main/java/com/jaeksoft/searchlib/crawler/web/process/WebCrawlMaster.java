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

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.AbstractManager;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem;
import com.jaeksoft.searchlib.crawler.web.database.NamedItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.pattern.PatternListMatcher;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapCache;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapUrl;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedHashSet;

public class WebCrawlMaster extends CrawlMasterAbstract<WebCrawlMaster, WebCrawlThread> {

	private final LinkedList<NamedItem> hostList;

	private volatile int maxUrlPerSession = 0;

	private final UrlCrawlQueue urlCrawlQueue;

	public WebCrawlMaster(Config config) throws SearchLibException, IOException {
		super(config);
		urlCrawlQueue = new UrlCrawlQueue(config);
		hostList = new LinkedList<>();
		if (config.getWebPropertyManager().getCrawlEnabled().getValue()) {
			Logging.info("Webcrawler is starting for " + config.getIndexName());
			start(false);
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		if (ClientFactory.INSTANCE.properties.isDisableWebCrawler()) {
			abort();
			propertyManager.getCrawlEnabled().setValue(false);
			throw new InterruptedException("The webcrawler is disabled.");
		}
		urlCrawlQueue.setMaxBufferSize(propertyManager.getIndexDocumentBufferSize().getValue());
		while (!isAborted()) {

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			urlCrawlQueue.setStatistiques(currentStats);

			final int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			maxUrlPerSession = propertyManager.getMaxUrlPerSession().getValue();
			final int maxUrlPerHost = propertyManager.getMaxUrlPerHost().getValue();
			final PatternListMatcher exclusionMatcher = propertyManager.getExclusionEnabled().getValue() ?
					config.getExclusionPatternManager().getPatternListMatcher() :
					null;
			final PatternListMatcher inclusionMatcher = propertyManager.getInclusionEnabled().getValue() ?
					config.getInclusionPatternManager().getPatternListMatcher() :
					null;
			final Integer maxDepth = propertyManager.getMaxDepth().getValue();
			String schedulerJobName = propertyManager.getSchedulerAfterSession().getValue();

			synchronized (hostList) {
				hostList.clear();
			}

			extractSiteMapList(inclusionMatcher, exclusionMatcher);
			extractHostList(maxUrlPerHost, maxDepth);

			while (!isAborted()) {

				int howMany = urlLeftPerHost(maxUrlPerHost);
				if (howMany <= 0)
					break;

				NamedItem host = getNextHost();
				if (host == null)
					break;

				HostUrlList hostUrlList = getNextUrlList(host, howMany, maxDepth);
				if (hostUrlList == null)
					continue;

				WebCrawlThread crawlThread = new WebCrawlThread(config, this, currentStats, hostUrlList);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}

			setStatus(CrawlStatus.WAITING_CHILD);
			while (getThreadsCount() > 0) {
				waitForChild(1800);
				if (isAborted())
					break;
			}
			setStatus(CrawlStatus.INDEXATION);
			urlCrawlQueue.index(true);
			if (schedulerJobName != null && schedulerJobName.length() > 0) {
				setStatus(CrawlStatus.EXECUTE_SCHEDULER_JOB);
				TaskManager.getInstance().executeJob(config.getIndexName(), schedulerJobName);
			}

			if (isOnce())
				break;
			sleepSec(5);
		}
		urlCrawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	private void extractHostList(final int maxUrlPerHost, final Integer maxDepth)
			throws IOException, ParseException, SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException, IllegalAccessException {
		Config config = getConfig();
		UrlManager urlManager = config.getUrlManager();
		setStatus(CrawlStatus.EXTRACTING_HOSTLIST);

		Set<String> hostSet = new TreeSet<String>();

		WebPropertyManager propertyManager = config.getWebPropertyManager();
		final Date fetchIntervalDate = AbstractManager.getPastDate(propertyManager.getFetchInterval().getValue(),
				propertyManager.getFetchIntervalUnit().getValue());

		int urlLimit = maxUrlPerSession;
		// First try fetch priority
		NamedItem.Selection selection =
				new NamedItem.Selection(ListType.PRIORITY_URL, FetchStatus.FETCH_FIRST, null, null);
		urlLimit = urlManager.getHostToFetch(selection, urlLimit, maxUrlPerHost, maxDepth, hostList, hostSet);

		// Second try old URLs
		selection = new NamedItem.Selection(ListType.OLD_URL, null, fetchIntervalDate, null);
		urlLimit = urlManager.getHostToFetch(selection, urlLimit, maxUrlPerHost, maxDepth, hostList, hostSet);

		// Finally try new unfetched URLs
		selection = new NamedItem.Selection(ListType.NEW_URL, FetchStatus.UN_FETCHED, null, fetchIntervalDate);
		urlLimit = urlManager.getHostToFetch(selection, urlLimit, maxUrlPerHost, maxDepth, hostList, hostSet);

		currentStats.addHostListSize(hostList.size());

	}

	private void extractSiteMapList(final PatternListMatcher inclusionMatcher,
			final PatternListMatcher exclusionMatcher) throws SearchLibException, IOException {
		HttpDownloader httpDownloader = null;
		try {
			httpDownloader = getNewHttpDownloader(true);
			final SiteMapList siteMapList = getConfig().getSiteMapList();
			final SiteMapCache siteMapCache = SiteMapCache.getInstance();
			if (siteMapList != null && siteMapList.getArray() != null) {
				setStatus(CrawlStatus.LOADING_SITEMAP);
				final UrlManager urlManager = getConfig().getUrlManager();
				final List<UrlItem> workInsertUrlList = new ArrayList<UrlItem>();
				for (SiteMapItem siteMap : siteMapList.getArray()) {
					final LinkedHashSet<SiteMapUrl> siteMapUrlSet = new LinkedHashSet<>();
					siteMap.fill(siteMapCache, getNewHttpDownloader(true), false, siteMapUrlSet);
					for (SiteMapUrl siteMapUrl : siteMapUrlSet) {

						final URI uri = siteMapUrl.getLoc();
						final String sUri = uri.toString();
						URL url;
						try {
							url = uri.toURL();
						} catch (MalformedURLException e) {
							continue;
						}

						if (exclusionMatcher != null)
							if (exclusionMatcher.matchPattern(url, sUri))
								continue;
						if (inclusionMatcher != null)
							if (!inclusionMatcher.matchPattern(url, sUri))
								continue;

						if (!urlManager.exists(sUri)) {
							workInsertUrlList.add(
									urlManager.getNewUrlItem(new LinkItem(sUri, LinkItem.Origin.sitemap, null, 0)));
						}
					}
				}
				if (workInsertUrlList.size() > 0)
					urlManager.updateUrlItems(workInsertUrlList);
			}
		} finally {
			if (httpDownloader != null)
				httpDownloader.release();
		}
	}

	public HttpDownloader getNewHttpDownloader(boolean followRedirect, String userAgent, boolean useProxies)
			throws SearchLibException, IOException {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		if (StringUtils.isEmpty(userAgent))
			userAgent = propertyManager.getUserAgent().getValue();
		return new HttpDownloader(userAgent, followRedirect, useProxies ? propertyManager.getProxyHandler() : null,
				propertyManager.getConnectionTimeOut().getValue() * 1000);
	}

	final public HttpDownloader getNewHttpDownloader(final boolean followRedirect)
			throws SearchLibException, IOException {
		return getNewHttpDownloader(followRedirect, null, true);
	}

	private NamedItem getNextHost() {
		synchronized (hostList) {
			int s = hostList.size();
			if (s > 0) {
				NamedItem host = hostList.remove(RandomUtils.nextInt(0, s));
				if (host != null) {
					currentStats.incHostCount();
					return host;
				}
			}
		}
		return null;
	}

	protected int urlLeft() {
		return (int) (maxUrlPerSession - currentStats.getFetchedCount());
	}

	private int urlLeftPerHost(int maxUrlPerHost) {
		int leftCount = urlLeft();
		if (leftCount < 0)
			return leftCount;
		if (leftCount > maxUrlPerHost)
			leftCount = maxUrlPerHost;
		return leftCount;
	}

	private HostUrlList getNextUrlList(final NamedItem host, final int count, final Integer maxDepth)
			throws ParseException, IOException, SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException, IllegalAccessException {

		setStatus(CrawlStatus.EXTRACTING_URLLIST);
		setInfo(host.getName());
		UrlManager urlManager = getConfig().getUrlManager();

		List<UrlItem> urlList = new ArrayList<UrlItem>();
		HostUrlList hostUrlList = new HostUrlList(urlList, host);
		hostUrlList.setListType(host.selection.listType);

		urlManager.getUrlToFetch(host, count, maxDepth, urlList);

		setInfo(null);
		return hostUrlList;
	}

	public boolean isFull() throws IOException {
		return currentStats.getFetchedCount() >= getConfig().getWebPropertyManager().getMaxUrlPerSession().getValue();
	}

	public Crawl getNewCrawl(WebCrawlThread crawlThread) throws SearchLibException, IOException {
		return new Crawl(crawlThread);

	}

	public WebCrawlThread manualCrawl(URL url, HostUrlList.ListType listType)
			throws SearchLibException, ParseException, IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		Config config = getConfig();
		if (currentStats == null)
			currentStats = new CrawlStatistics();
		UrlManager urlManager = config.getUrlManager();
		List<UrlItem> urlItemList = new ArrayList<UrlItem>();
		UrlItem urlItem = urlManager.getUrlToFetch(url);
		if (urlItem == null)
			urlItem = urlManager.getNewUrlItem(new LinkItem(url.toExternalForm(), LinkItem.Origin.manual, null, 0));
		urlItemList.add(urlItem);
		HostUrlList hostUrlList = new HostUrlList(urlItemList, new NamedItem(url.getHost()));
		hostUrlList.setListType(listType);
		WebCrawlThread crawlThread = new WebCrawlThread(config, this, new CrawlStatistics(), hostUrlList);
		crawlThread.execute(180);
		return crawlThread;

	}

	public CrawlQueueAbstract getCrawlQueue() {
		return urlCrawlQueue;
	}

	@Override
	protected WebCrawlThread[] getNewArray(int size) {
		return new WebCrawlThread[size];
	}

}
