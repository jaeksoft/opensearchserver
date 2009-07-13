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

import java.io.File;
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
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.PathItem;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlThread;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.plugin.IndexPluginList;

public class CrawlFileMaster extends CrawlThreadAbstract {

	private final LinkedHashSet<CrawlFileThread> crawlThreads;

	private CrawlThread[] crawlThreadArray;

	private final LinkedList<CrawlStatistics> statistics;

	private final Config config;

	private IndexPluginList indexPluginList;

	private CrawlStatistics sessionStats;

	private FileCrawlQueue crawlQueue;

	private final ExecutorService threadPool;

	private final Date fetchIntervalDate;

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

		FileManager manager = config.getFileManager();
		PropertyManager propertyManager = config.getPropertyManager();

		fetchIntervalDate = manager.getPastDate(propertyManager
				.getFetchInterval());
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

	private void add(CrawlFileThread crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.add(crawlThread);
			crawlThreadArray = null;
		}
		crawlThread.start(threadPool);
	}

	private void addChildRec(File file, String originalPath, boolean recursive)
			throws SearchLibException {
		File[] children = file.listFiles();
		if (children != null && children.length > 0) {
			for (File current : children) {
				if (current.isDirectory() && recursive)
					addChildRec(current, originalPath, true);
				else if (current.isFile()) {
					CrawlFileThread crawlThread = new CrawlFileThread(config,
							this, sessionStats, new FileItem(current.getPath(),
									originalPath));
					add(crawlThread);
				}
			}
		}
	}

	private void addChildrenToCrawl(PathItem item) throws SearchLibException {
		String originalPath = item.getPath();
		File root = new File(item.getPath());

		if (root.isFile()) {
			CrawlFileThread crawlThread = new CrawlFileThread(config, this,
					sessionStats, new FileItem(root.getPath(), originalPath));
			add(crawlThread);

		} else if (root.isDirectory()) {
			// Add its children and children of children
			if (item.isWithSub())
				addChildRec(root, originalPath, true);

			// Only add its children
			else
				addChildRec(root, originalPath, false);
		}
	}

	private void addStatistics(CrawlStatistics stats) {
		synchronized (statistics) {
			if (statistics.size() >= 10)
				statistics.removeLast();
			statistics.addFirst(stats);
		}
	}

	@Override
	public void complete() {
	}

	private int crawlThreadsSize() {
		synchronized (crawlThreads) {
			return crawlThreads.size();
		}
	}

	protected FileCrawlQueue getCrawlQueue() {
		return crawlQueue;
	}

	public CrawlThread[] getCrawlThreads() {
		synchronized (crawlThreads) {
			if (crawlThreadArray != null)
				return crawlThreadArray;
			crawlThreadArray = new CrawlThread[crawlThreads.size()];
			return crawlThreads.toArray(crawlThreadArray);
		}
	}

	public IndexPluginList getIndexPluginList() {
		return indexPluginList;
	}

	private List<PathItem> getNextPathList(int count) throws ParseException,
			IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {

		List<PathItem> fileList = new ArrayList<PathItem>();
		config.getFilePathManager().getPaths("", 0, count, fileList);

		setInfo(null);
		return fileList;
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	public boolean isFull() throws SearchLibException {
		return sessionStats.getFetchedCount() >= config.getPropertyManager()
				.getMaxUrlPerSession();
	}

	protected void remove(CrawlFileThread crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.remove(crawlThread);
			crawlThreadArray = null;
		}
	}

	protected void remove(CrawlThread crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.remove(crawlThread);
			crawlThreadArray = null;
		}
	}

	@Override
	public void runner() throws Exception {
		PropertyManager propertyManager = config.getPropertyManager();
		while (!isAbort()) {

			int threadNumber = propertyManager.getMaxThreadNumber();

			sessionStats = new CrawlStatistics();
			addStatistics(sessionStats);
			crawlQueue.setStatistiques(sessionStats);

			setStatus(CrawlStatus.STARTING);

			List<PathItem> pathList = getNextPathList(10);
			if (pathList == null)
				continue;

			setStatus(CrawlStatus.CRAWL);
			config.getFileManager().reload(true);

			Iterator<PathItem> it = pathList.iterator();
			while (!isAbort() && it.hasNext()) {

				PathItem item = it.next();
				addChildrenToCrawl(item);
				System.out.println("Crawl size " + crawlThreadsSize());
				while (crawlThreadsSize() >= threadNumber && !isAbort())
					sleepSec(5);
			}

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

	public void start() {
		if (isRunning())
			return;
		try {
			crawlQueue = new FileCrawlQueue(config);
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
}
