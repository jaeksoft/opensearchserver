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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.AbstractManager;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem;
import com.jaeksoft.searchlib.crawler.web.database.NamedItem;
import com.jaeksoft.searchlib.crawler.web.database.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.database.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.util.DomUtils;

public class WebCrawlMaster extends CrawlMasterAbstract {

	private final LinkedList<NamedItem> oldHostList;

	private final LinkedList<NamedItem> newHostList;

	private Date fetchIntervalDate;

	private int maxUrlPerSession;

	private int maxUrlPerHost;

	private UrlCrawlQueue urlCrawlQueue;

	public WebCrawlMaster(Config config) throws SearchLibException {
		super(config);
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		urlCrawlQueue = new UrlCrawlQueue(config, propertyManager);
		oldHostList = new LinkedList<NamedItem>();
		newHostList = new LinkedList<NamedItem>();
		if (propertyManager.getCrawlEnabled().getValue()) {
			Logging.info("Webcrawler is starting for " + config.getIndexName());
			start(false);
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		while (!isAborted()) {

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			urlCrawlQueue.setStatistiques(currentStats);

			int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			maxUrlPerSession = propertyManager.getMaxUrlPerSession().getValue();
			maxUrlPerHost = propertyManager.getMaxUrlPerHost().getValue();
			String schedulerJobName = propertyManager
					.getSchedulerAfterSession().getValue();

			synchronized (newHostList) {
				newHostList.clear();
			}
			synchronized (oldHostList) {
				oldHostList.clear();
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

				CrawlThreadAbstract crawlThread = new WebCrawlThread(config,
						this, currentStats, hostUrlList);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}

			waitForChild(600);
			setStatus(CrawlStatus.INDEXATION);
			urlCrawlQueue.index(true);
			if (currentStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTIMIZATION);
				config.getUrlManager().reload(true, null);
			}
			if (schedulerJobName != null && schedulerJobName.length() > 0) {
				setStatus(CrawlStatus.EXECUTE_SCHEDULER_JOB);
				TaskManager.executeJob(config.getIndexName(), schedulerJobName);
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
		urlManager.getOldHostToFetch(fetchIntervalDate, maxUrlPerSession,
				oldHostList);
		currentStats.addOldHostListSize(oldHostList.size());
		urlManager.getNewHostToFetch(fetchIntervalDate, maxUrlPerSession,
				newHostList);
		currentStats.addNewHostListSize(newHostList.size());
	}

	private void extractSiteMapList() throws SearchLibException {
		SiteMapList siteMapList = getConfig().getSiteMapList();

		if (siteMapList != null && siteMapList.getArray() != null) {
			UrlManager urlManager = getConfig().getUrlManager();
			List<UrlItem> workInsertUrlList = new ArrayList<UrlItem>();
			for (SiteMapItem siteMap : siteMapList.getArray()) {
				List<String> urls = getListOfUrls(siteMap.getUri());
				for (String uri : urls) {
					if (!urlManager.exists(uri)) {
						workInsertUrlList.add(urlManager
								.getNewUrlItem(new LinkItem(uri,
										LinkItem.Origin.sitemap, null)));
					}
				}
			}
			if (workInsertUrlList.size() > 0)
				urlManager.updateUrlItems(workInsertUrlList);
		}
	}

	private List<String> getListOfUrls(String uri) {
		List<String> urls = new ArrayList<String>();
		try {
			// parse using builder to get DOM representation of the XML file
			Document doc = DomUtils.readXml(new InputSource(uri), true);
			if (doc != null) {
				List<Node> nodes = DomUtils.getAllNodes(doc, "loc");
				if (nodes != null) {
					for (Node node : nodes) {
						String href = DomUtils.getText(node);
						if (href != null && !href.equalsIgnoreCase("")) {
							// check url format
							URL newUrl = new URL(href);
							urls.add(newUrl.toExternalForm());
						}
					}
				}
			}
		} catch (Exception ex) {
			Logging.warn(ex);
		}
		return urls;
	}

	private NamedItem getNextHost() {
		synchronized (oldHostList) {
			int s = oldHostList.size();
			if (s > 0) {
				NamedItem host = oldHostList.remove(new Random().nextInt(s));
				if (host != null) {
					host.setList(oldHostList);
					currentStats.incOldHostCount();
					return host;
				}
			}
		}
		synchronized (newHostList) {
			int s = newHostList.size();
			if (s > 0) {
				NamedItem host = newHostList.remove(new Random().nextInt(s));
				if (host != null) {
					host.setList(newHostList);
					currentStats.incNewHostCount();
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
		if (host.getList() == oldHostList) {
			hostUrlList.setListType(ListType.OLD_URL);
			urlManager
					.getOldUrlToFetch(host, fetchIntervalDate, count, urlList);
		} else if (host.getList() == newHostList) {
			hostUrlList.setListType(ListType.NEW_URL);
			urlManager
					.getNewUrlToFetch(host, fetchIntervalDate, count, urlList);
		}
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

	@Override
	public CrawlQueueAbstract getCrawlQueue() {
		return urlCrawlQueue;
	}
}
