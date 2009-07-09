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

package com.jaeksoft.searchlib.crawler.file.process;

import java.lang.Thread.State;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueue;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlThread;
import com.jaeksoft.searchlib.plugin.IndexPluginList;

public class CrawlFileMaster extends CrawlThreadAbstract {

	private final LinkedHashSet<CrawlFileThread> crawlThreads;

	private CrawlThread[] crawlThreadArray;

	private final LinkedList<CrawlStatistics> statistics;

	private final Config config;

	private IndexPluginList indexPluginList;

	private CrawlStatistics sessionStats;

	private CrawlQueue crawlQueue;

	private final ExecutorService threadPool;

	public CrawlFileMaster(Config config) throws SearchLibException {
		this.config = config;
		threadPool = Executors.newCachedThreadPool();
		crawlThreads = new LinkedHashSet<CrawlFileThread>();
		crawlThreadArray = null;
		crawlQueue = null;
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

			add(new CrawlFileThread(config, this, sessionStats));

			waitForChild();
			setStatus(CrawlStatus.INDEXATION);
			crawlQueue.index(true);
			if (sessionStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTMIZING_INDEX);
				config.getFileManager().reload(
						propertyManager.isOptimizeAfterSession());
			}
			sleepSec(5);
		}
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	private int crawlThreadsSize() {
		synchronized (crawlThreads) {
			return crawlThreads.size();
		}
	}

	private void add(CrawlFileThread crawlThread) {
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

	protected void remove(CrawlFileThread crawlThread) {
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
					Iterator<CrawlFileThread> it = crawlThreads.iterator();
					while (it.hasNext()) {
						CrawlFileThread crawlThread = it.next();
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
				for (CrawlFileThread crawlThread : crawlThreads)
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
