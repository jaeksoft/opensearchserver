/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.NamedItem;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.plugin.IndexPluginList;

public class CrawlMaster extends CrawlThreadAbstract {

	private LinkedHashSet<CrawlThread> crawlThreads;

	private CrawlThread[] crawlThreadArray;

	private LinkedList<NamedItem> oldHostList;

	private LinkedList<NamedItem> newHostList;

	private LinkedList<CrawlStatistics> statistics;

	private Config config;

	private IndexPluginList indexPluginList;

	private CrawlStatistics sessionStats;

	private CrawlQueue crawlQueue;

	private Date fetchIntervalDate;

	private ExecutorService threadPool;

	private int maxUrlPerSession;

	private int maxUrlPerHost;

	public CrawlMaster(Config config) throws SearchLibException {
		this.config = config;
		threadPool = Executors.newCachedThreadPool();
		crawlThreads = new LinkedHashSet<CrawlThread>();
		crawlThreadArray = null;
		crawlQueue = null;
		oldHostList = new LinkedList<NamedItem>();
		newHostList = new LinkedList<NamedItem>();
		statistics = new LinkedList<CrawlStatistics>();
		sessionStats = null;
		if (config.getPropertyManager().isCrawlEnabled())
			start();
	}

	public void start() {
		if (isRunning())
			return;
		try {
			crawlQueue = new CrawlQueue(config);
			setStatus(CrawlStatus.STARTING);
			indexPluginList = new IndexPluginList(config
					.getIndexPluginTemplateList());
		} catch (SearchLibException e) {
			e.printStackTrace();
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage());
			return;
		}
		super.start(threadPool);
	}

	private void addStatistics(CrawlStatistics stats) {
		synchronized (statistics) {
			if (statistics.size() >= 10)
				statistics.removeLast();
			statistics.addFirst(stats);
		}
	}

	@Override
	public void runner() throws Exception {
		PropertyManager propertyManager = config.getPropertyManager();
		while (!isAbort()) {

			sessionStats = new CrawlStatistics();
			addStatistics(sessionStats);
			crawlQueue.setStatistiques(sessionStats);

			int threadNumber = propertyManager.getMaxThreadNumber();
			maxUrlPerSession = propertyManager.getMaxUrlPerSession();
			maxUrlPerHost = propertyManager.getMaxUrlPerHost();

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

				CrawlThread crawlThread = new CrawlThread(config, this,
						sessionStats, urlList, host);
				add(crawlThread);

				while (crawlThreadsSize() >= threadNumber && !isAbort())
					sleepSec(5);
			}

			waitForChild();
			setStatus(CrawlStatus.INDEXATION);
			crawlQueue.index(true);
			if (sessionStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTMIZING_INDEX);
				config.getUrlManager().reload(
						propertyManager.isOptimizeAfterSession());
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
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	private void extractHostList() throws IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException {
		setStatus(CrawlStatus.EXTRACTING_HOSTLIST);
		UrlManager urlManager = config.getUrlManager();
		PropertyManager propertyManager = config.getPropertyManager();
		fetchIntervalDate = urlManager.getPastDate(propertyManager
				.getFetchInterval());
		config.getUrlManager().getOldHostToFetch(fetchIntervalDate,
				maxUrlPerSession, oldHostList);
		sessionStats.addOldHostListSize(oldHostList.size());
		config.getUrlManager().getNewHostToFetch(maxUrlPerSession, newHostList);
		sessionStats.addNewHostListSize(newHostList.size());
	}

	private NamedItem getNextHost() {
		synchronized (oldHostList) {
			NamedItem host = oldHostList.poll();
			if (host != null) {
				host.setList(oldHostList);
				sessionStats.incOldHostCount();
				return host;
			}
		}
		synchronized (newHostList) {
			NamedItem host = newHostList.poll();
			if (host != null) {
				host.setList(newHostList);
				sessionStats.incNewHostCount();
				return host;
			}
		}
		return null;
	}

	protected int urlLeft() {
		return (int) (maxUrlPerSession - sessionStats.getFetchedCount());
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
			SearchLibException {

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

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	private int crawlThreadsSize() {
		synchronized (crawlThreads) {
			return crawlThreads.size();
		}
	}

	private void add(CrawlThread crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.add(crawlThread);
			crawlThreadArray = null;
		}
		crawlThread.start(threadPool);
	}

	protected void remove(CrawlThread crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.remove(crawlThread);
			crawlThreadArray = null;
		}
	}

	private void waitForChild() {
		while (crawlThreadsSize() > 0) {
			try {
				synchronized (this) {
					wait(5000);
				}
				// Remove terminated thread
				synchronized (crawlThreads) {
					boolean remove = false;
					Iterator<CrawlThread> it = crawlThreads.iterator();
					while (it.hasNext()) {
						CrawlThread crawlThread = it.next();
						if (crawlThread.getThreadState() == State.TERMINATED) {
							it.remove();
							remove = true;
						} else if (crawlThread.getCrawlTimeOutExhausted(300)) {
							crawlThread.abort();
							it.remove();
							remove = true;
						}
					}
					if (remove)
						crawlThreadArray = null;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sleepSec(1);
		}
	}

	@Override
	public void abort() {
		synchronized (this) {
			synchronized (crawlThreads) {
				for (CrawlThread crawlThread : crawlThreads)
					crawlThread.abort();
			}
			super.abort();
		}
	}

	public CrawlThread[] getCrawlThreads() {
		synchronized (crawlThreads) {
			if (crawlThreadArray != null)
				return crawlThreadArray;
			crawlThreadArray = new CrawlThread[crawlThreads.size()];
			return crawlThreads.toArray(crawlThreadArray);
		}
	}

	public boolean isFull() throws SearchLibException {
		return sessionStats.getFetchedCount() >= config.getPropertyManager()
				.getMaxUrlPerSession();
	}

	public IndexPluginList getIndexPluginList() {
		return indexPluginList;
	}

	protected CrawlQueue getCrawlQueue() {
		return crawlQueue;
	}

	@Override
	public void complete() {
	}

}
