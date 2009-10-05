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
import java.io.UnsupportedEncodingException;
import java.lang.Thread.State;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.database.PathItem;
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

	private int delayBetweenAccess;

	public CrawlFileMaster(Config config) throws SearchLibException {
		this.config = config;
		threadPool = Executors.newCachedThreadPool();
		crawlThreads = new LinkedHashSet<CrawlFileThread>();
		crawlThreadArray = null;
		crawlQueue = null;
		statistics = new LinkedList<CrawlStatistics>();
		sessionStats = null;
		if (config.getFilePropertyManager().getCrawlEnabled().getValue())
			start();
		delayBetweenAccess = 100;
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

	/**
	 * Add to crawl queue if needed
	 * 
	 * @throws java.text.ParseException
	 * 
	 */
	private void sendToCrawl(File current, URI originalUri)
			throws CorruptIndexException, SearchLibException, ParseException,
			UnsupportedEncodingException, java.text.ParseException {

		FileItem fileItem = config.getFileManager().find(
				current.toURI().toASCIIString());

		// Crawl
		if (fileItem == null
				|| (fileItem != null && fileItem.isNewCrawlNeeded(current
						.lastModified()))) {
			fileItem = new FileItem(current.toURI(), current.getParentFile()
					.toURI(), originalUri, current.lastModified(), current
					.length());

			add(new CrawlFileThread(config, this, sessionStats, fileItem));
			sleepMs(delayBetweenAccess, false);
		}
	}

	/**
	 * Start of the recursive crawl
	 * 
	 * @throws java.text.ParseException
	 * 
	 */
	private void addChildrenToCrawl(PathItem item) throws SearchLibException,
			CorruptIndexException, ParseException,
			UnsupportedEncodingException, java.text.ParseException {

		if (!isAbort()) {
			File current = new File(item.getPath());
			if (current.isFile()) {
				sendToCrawl(current, current.toURI());
			} else if (current.isDirectory()) {
				// Add its children
				addChildRec(current, current.toURI(), item.isWithSub());
			}
		}
	}

	/**
	 * Recursive loop to crawl directories to leaves
	 * 
	 * @throws java.text.ParseException
	 * 
	 */
	private void addChildRec(File file, URI originalUri, boolean recursive)
			throws SearchLibException, CorruptIndexException, ParseException,
			UnsupportedEncodingException, java.text.ParseException {

		if (!isAbort()) {
			sleepMs(delayBetweenAccess, false);

			List<URI> uriName = new ArrayList<URI>();
			File[] children = file.listFiles();

			if (children != null && children.length > 0) {
				for (File current : children) {
					if (current.isDirectory() && recursive) {
						addChildRec(current, originalUri, true);
					}

					if (!isAbort()) {
						uriName.add(current.toURI());
						sendToCrawl(current, originalUri);
					}
				}

				// Looking for deleted files
				checkDeleteFiles(file.toURI(), uriName);
			}
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

	private List<PathItem> getNextPathList() throws ParseException,
			IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {

		List<PathItem> fileList = new ArrayList<PathItem>();
		config.getFilePathManager().getAllPaths(fileList);
		return fileList;
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
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
		FilePropertyManager propertyManager = config.getFilePropertyManager();

		crawlQueue.setMaxBufferSize(propertyManager
				.getIndexDocumentBufferSize().getValue());

		while (!isAbort()) {

			int threadNumber = 1;
			delayBetweenAccess = propertyManager.getDelayBetweenAccesses()
					.getValue();

			sessionStats = new CrawlStatistics();
			addStatistics(sessionStats);
			crawlQueue.setStatistiques(sessionStats);

			setStatus(CrawlStatus.STARTING);

			List<PathItem> pathList = getNextPathList();
			if (pathList == null)
				continue;

			setStatus(CrawlStatus.CRAWL);
			config.getFileManager().reload(true);

			Iterator<PathItem> it = pathList.iterator();
			while (!isAbort() && it.hasNext()) {
				addChildrenToCrawl((PathItem) it.next());

				while (crawlThreadsSize() >= threadNumber && !isAbort())
					sleepSec(5);
			}

			waitForChild();
			crawlQueue.index(true);
			setStatus(CrawlStatus.INDEXATION);

			if (sessionStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTMIZING_INDEX);
				config.getFileManager().reload(
						propertyManager.getOptimizeAfterSession().getValue());
			}

			sleepSec(5);
		}
		crawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	public final void checkDeleteFiles(URI parentUri, List<URI> children)
			throws SearchLibException, CorruptIndexException,
			UnsupportedEncodingException, ParseException {

		if (parentUri == null)
			return;

		List<FileItem> indexFiles = config.getFileManager()
				.findAllByDirectoryURI(parentUri);

		Iterator<FileItem> indexIterator = indexFiles.iterator();
		Iterator<URI> fileIterator = children.iterator();

		URI file = null;
		FileItem indexFile = null;

		while (indexIterator.hasNext() && fileIterator.hasNext() && !isAbort()) {

			if (file == null && fileIterator.hasNext())
				file = fileIterator.next();

			if (indexFile == null && indexIterator.hasNext())
				indexFile = indexIterator.next();

			if (file != null && indexFile != null && indexFile.uri != null) {

				// indexFile before file
				if (indexFile.uri.compareTo(file) < 0) {
					getCrawlQueue().delete(indexFile.uri.toASCIIString());
					indexFile = null;
				}
				// indexFile after file
				else if (indexFile.uri.compareTo(file) > 0) {
					file = null;
				}
				// equals : both next
				else {
					indexFile = null;
					file = null;
				}
			}
			// Only files in index
			else if (file == null && indexFile != null && indexFile.uri != null) {
				getCrawlQueue().delete(indexFile.uri.toASCIIString());
				indexFile = null;

			} else {
				break;
			}
		}
	}

	public void start() {
		if (isRunning())
			return;
		try {
			crawlQueue = new FileCrawlQueue(config);
			setStatus(CrawlStatus.STARTING);
			indexPluginList = new IndexPluginList(config
					.getIndexPluginTemplateList());
			setInfo(null);
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

	/**
	 * Delete order to crawlQueue.
	 * 
	 * @param uri
	 * @return
	 */
	public boolean deleteToCrawlQueue(URI uri) {
		if (crawlQueue != null) {
			crawlQueue.deleteByOriginalUri(uri.toASCIIString());
			return true;
		}
		return false;
	}

}
