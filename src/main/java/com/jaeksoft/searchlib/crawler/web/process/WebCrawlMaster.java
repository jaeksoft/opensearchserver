/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapUrl;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskManager;

public class WebCrawlMaster extends
		CrawlMasterAbstract<WebCrawlMaster, WebCrawlThread> {

	private final LinkedList<NamedItem> hostList;

	private Date fetchIntervalDate;

	public class Selection {

		private final ListType listType;

		private final Date beforeDate;

		private final Date afterDate;

		private final FetchStatus fetchStatus;

		public Selection(ListType listType, FetchStatus fetchStatus,
				Date beforeDate, Date afterDate) {
			this.listType = listType;
			this.fetchStatus = fetchStatus;
			this.beforeDate = beforeDate;
			this.afterDate = afterDate;
		}
	}

	private Selection selection;

	private int maxUrlPerSession;

	private int maxUrlPerHost;

	private UrlCrawlQueue urlCrawlQueue;

	public WebCrawlMaster(Config config) throws SearchLibException {
		super(config);
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		urlCrawlQueue = new UrlCrawlQueue(config);
		hostList = new LinkedList<NamedItem>();
		if (propertyManager.getCrawlEnabled().getValue()) {
			Logging.info("Webcrawler is starting for " + config.getIndexName());
			start(false);
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		urlCrawlQueue.setMaxBufferSize(propertyManager
				.getIndexDocumentBufferSize().getValue());
		while (!isAborted()) {

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			urlCrawlQueue.setStatistiques(currentStats);

			int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			maxUrlPerSession = propertyManager.getMaxUrlPerSession().getValue();
			maxUrlPerHost = propertyManager.getMaxUrlPerHost().getValue();
			String schedulerJobName = propertyManager
					.getSchedulerAfterSession().getValue();

			synchronized (hostList) {
				hostList.clear();
			}

			extractSiteMapList();
			extractHostList();

			while (!isAborted()) {

				int howMany = urlLeftPerHost();
				if (howMany <= 0)
					break;

				NamedItem host = getNextHost();
				if (host == null)
					break;

				HostUrlList hostUrlList = getNextUrlList(host, howMany);
				if (hostUrlList == null)
					continue;

				WebCrawlThread crawlThread = new WebCrawlThread(config, this,
						currentStats, hostUrlList);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}

			setStatus(CrawlStatus.WAITING_CHILD);
			waitForChild(1800);
			setStatus(CrawlStatus.INDEXATION);
			urlCrawlQueue.index(true);
			if (currentStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTIMIZATION);
				config.getUrlManager().reload(true, null);
			}
			if (schedulerJobName != null && schedulerJobName.length() > 0) {
				setStatus(CrawlStatus.EXECUTE_SCHEDULER_JOB);
				TaskManager.getInstance().executeJob(config.getIndexName(),
						schedulerJobName);
			}

			if (isOnce())
				break;
			sleepSec(5);
		}
		urlCrawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	private void extractHostList() throws IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException,
			IllegalAccessException {
		Config config = getConfig();
		UrlManager urlManager = config.getUrlManager();
		setStatus(CrawlStatus.OPTIMIZATION);
		urlManager.reload(true, null);
		setStatus(CrawlStatus.EXTRACTING_HOSTLIST);

		WebPropertyManager propertyManager = config.getWebPropertyManager();
		fetchIntervalDate = AbstractManager.getPastDate(propertyManager
				.getFetchInterval().getValue(), propertyManager
				.getFetchIntervalUnit().getValue());

		// First try fetch priority
		selection = new Selection(ListType.PRIORITY_URL,
				FetchStatus.FETCH_FIRST, null, null);
		urlManager.getHostToFetch(selection.fetchStatus, selection.beforeDate,
				selection.afterDate, maxUrlPerSession, hostList);

		// Second try old URLs
		if (hostList.size() == 0) {
			selection = new Selection(ListType.OLD_URL, null,
					fetchIntervalDate, null);
			urlManager.getHostToFetch(selection.fetchStatus,
					selection.beforeDate, selection.afterDate,
					maxUrlPerSession, hostList);
		}

		// Finally try new unfetched URLs
		if (hostList.size() == 0) {
			selection = new Selection(ListType.NEW_URL, FetchStatus.UN_FETCHED,
					null, fetchIntervalDate);
			urlManager.getHostToFetch(selection.fetchStatus,
					selection.beforeDate, selection.afterDate,
					maxUrlPerSession, hostList);
		}
		currentStats.addHostListSize(hostList.size());

	}

	private void extractSiteMapList() throws SearchLibException {
		HttpDownloader httpDownloader = null;
		try {
			httpDownloader = getNewHttpDownloader(true);
			SiteMapList siteMapList = getConfig().getSiteMapList();
			if (siteMapList != null && siteMapList.getArray() != null) {
				setStatus(CrawlStatus.LOADING_SITEMAP);
				UrlManager urlManager = getConfig().getUrlManager();
				List<UrlItem> workInsertUrlList = new ArrayList<UrlItem>();
				for (SiteMapItem siteMap : siteMapList.getArray()) {
					Set<SiteMapUrl> siteMapUrlSet = siteMap.load(
							getNewHttpDownloader(true), null);
					for (SiteMapUrl siteMapUrl : siteMapUrlSet) {
						String sUri = siteMapUrl.getLoc().toString();
						if (!urlManager.exists(sUri)) {
							workInsertUrlList.add(urlManager
									.getNewUrlItem(new LinkItem(sUri,
											LinkItem.Origin.sitemap, null)));
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

	public HttpDownloader getNewHttpDownloader(boolean followRedirect,
			String userAgent) throws SearchLibException {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		return new HttpDownloader(userAgent, followRedirect,
				propertyManager.getProxyHandler());
	}

	public HttpDownloader getNewHttpDownloader(boolean followRedirect)
			throws SearchLibException {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		return getNewHttpDownloader(followRedirect, propertyManager
				.getUserAgent().getValue());
	}

	private NamedItem getNextHost() {
		synchronized (hostList) {
			int s = hostList.size();
			if (s > 0) {
				NamedItem host = hostList.remove(new Random().nextInt(s));
				if (host != null) {
					host.setList(hostList);
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

	private int urlLeftPerHost() {
		int leftCount = urlLeft();
		if (leftCount < 0)
			return leftCount;
		if (leftCount > maxUrlPerHost)
			leftCount = maxUrlPerHost;
		return leftCount;
	}

	private HostUrlList getNextUrlList(NamedItem host, int count)
			throws ParseException, IOException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {

		setStatus(CrawlStatus.EXTRACTING_URLLIST);
		setInfo(host.getName());
		UrlManager urlManager = getConfig().getUrlManager();

		List<UrlItem> urlList = new ArrayList<UrlItem>();
		HostUrlList hostUrlList = new HostUrlList(urlList, host);
		hostUrlList.setListType(selection.listType);

		urlManager.getUrlToFetch(host, selection.fetchStatus,
				selection.beforeDate, selection.afterDate, count, urlList);

		setInfo(null);
		return hostUrlList;
	}

	public boolean isFull() throws SearchLibException {
		return currentStats.getFetchedCount() >= getConfig()
				.getWebPropertyManager().getMaxUrlPerSession().getValue();
	}

	public Crawl getNewCrawl(WebCrawlThread crawlThread)
			throws SearchLibException {
		return new Crawl(crawlThread);

	}

	public WebCrawlThread manualCrawl(URL url, HostUrlList.ListType listType)
			throws SearchLibException, ParseException, IOException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		Config config = getConfig();
		UrlManager urlManager = config.getUrlManager();
		List<UrlItem> urlItemList = new ArrayList<UrlItem>();
		UrlItem urlItem = urlManager.getUrlToFetch(url);
		if (urlItem == null)
			urlItem = urlManager.getNewUrlItem(new LinkItem(url
					.toExternalForm(), LinkItem.Origin.manual, null));
		urlItemList.add(urlItem);
		HostUrlList hostUrlList = new HostUrlList(urlItemList, new NamedItem(
				url.getHost()));
		hostUrlList.setListType(listType);
		WebCrawlThread crawlThread = new WebCrawlThread(config, this,
				new CrawlStatistics(), hostUrlList);
		crawlThread.execute();
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
