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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.web.database.NamedItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlCrawlQueue;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.function.expression.SyntaxError;

public class WebCrawlMaster extends CrawlMasterAbstract {

	private final LinkedList<NamedItem> oldHostList;

	private final LinkedList<NamedItem> newHostList;

	private Date fetchIntervalDate;

	private int maxUrlPerSession;

	private int maxUrlPerHost;

	public WebCrawlMaster(Config config) throws SearchLibException {
		super(config);
		crawlQueue = null;
		oldHostList = new LinkedList<NamedItem>();
		newHostList = new LinkedList<NamedItem>();
		if (config.getWebPropertyManager().getCrawlEnabled().getValue()) {
			System.out.println("Webcrawler is starting for "
					+ config.getIndexDirectory().getName());
			start();
		}
	}

	@Override
	public void runner() throws Exception {
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		while (!isAbort()) {

			crawlQueue = new UrlCrawlQueue(config, propertyManager);

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			crawlQueue.setStatistiques(currentStats);

			int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			maxUrlPerSession = propertyManager.getMaxUrlPerSession().getValue();
			maxUrlPerHost = propertyManager.getMaxUrlPerHost().getValue();

			synchronized (newHostList) {
				newHostList.clear();
			}
			synchronized (oldHostList) {
				oldHostList.clear();
			}
			extractHostList();

			while (!isAbort()) {

				int howMany = urlLeftPerHost();
				if (howMany <= 0)
					break;

				NamedItem host = getNextHost();
				if (host == null)
					break;

				List<UrlItem> urlList = getNextUrlList(host, howMany);
				if (urlList == null)
					continue;

				CrawlThreadAbstract crawlThread = new WebCrawlThread(config,
						this, currentStats, urlList, host);
				add(crawlThread);

				while (crawlThreadsSize() >= threadNumber && !isAbort())
					sleepSec(5);
			}

			waitForChild();
			setStatus(CrawlStatus.INDEXATION);
			crawlQueue.index(true);
			if (currentStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTMIZING_INDEX);
				config.getUrlManager().reload(
						propertyManager.getOptimizeAfterSession().getValue());
				// TEMP publishIndexList disabled
				/*
				 * PublishIndexList publishIndexList = client
				 * .getPublishIndexList(); if (publishIndexList != null &&
				 * publishIndexList.size() > 0 &&
				 * propertyManager.isPublishAfterSession()) {
				 * setStatus(CrawlStatus.PUBLISH_INDEX);
				 * publishIndexList.publish(); }
				 */
			}
			sleepSec(5);
		}
		crawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	private void extractHostList() throws IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException,
			IllegalAccessException {
		setStatus(CrawlStatus.EXTRACTING_HOSTLIST);
		UrlManager urlManager = config.getUrlManager();
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		fetchIntervalDate = urlManager.getPastDate(propertyManager
				.getFetchInterval().getValue());
		config.getUrlManager().getOldHostToFetch(fetchIntervalDate,
				maxUrlPerSession, oldHostList);
		currentStats.addOldHostListSize(oldHostList.size());
		config.getUrlManager().getNewHostToFetch(maxUrlPerSession, newHostList);
		currentStats.addNewHostListSize(newHostList.size());
	}

	private NamedItem getNextHost() {
		synchronized (oldHostList) {
			NamedItem host = oldHostList.poll();
			if (host != null) {
				host.setList(oldHostList);
				currentStats.incOldHostCount();
				return host;
			}
		}
		synchronized (newHostList) {
			NamedItem host = newHostList.poll();
			if (host != null) {
				host.setList(newHostList);
				currentStats.incNewHostCount();
				return host;
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

	private List<UrlItem> getNextUrlList(NamedItem host, int count)
			throws ParseException, IOException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {

		setStatus(CrawlStatus.EXTRACTING_URLLIST);
		setInfo(host.name);
		UrlManager urlManager = config.getUrlManager();

		List<UrlItem> urlList = new ArrayList<UrlItem>();
		if (host.list == oldHostList)
			urlManager
					.getOldUrlToFetch(host, fetchIntervalDate, count, urlList);
		else if (host.list == newHostList)
			urlManager.getNewUrlToFetch(host, count, urlList);
		setInfo(null);
		return urlList;
	}

	public boolean isFull() throws SearchLibException {
		return currentStats.getFetchedCount() >= config.getWebPropertyManager()
				.getMaxUrlPerSession().getValue();
	}

}
