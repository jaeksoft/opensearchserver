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

import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.crawler.web.database.PropertyManager;

public class CrawlFileThread extends CrawlThreadAbstract {

	private final Config config;
	private final CrawlFileMaster crawlMaster;
	private FileItem currentFileItem;
	private final CrawlStatistics currentStats;
	private final long delayBetweenAccesses;
	private long nextTimeTarget;

	protected CrawlFileThread(Config config, CrawlFileMaster crawlMaster,
			CrawlStatistics sessionStats, List<FileItem> listFile)
			throws SearchLibException {

		this.config = config;
		this.crawlMaster = crawlMaster;
		currentStats = new CrawlStatistics(sessionStats);
		delayBetweenAccesses = config.getPropertyManager()
				.getDelayBetweenAccesses();
		nextTimeTarget = 0;
	}

	public CrawlFileThread(Config config, CrawlFileMaster crawlMaster,
			CrawlStatistics sessionStats, FileItem item)
			throws SearchLibException {

		this.config = config;
		this.crawlMaster = crawlMaster;
		currentStats = new CrawlStatistics(sessionStats);
		delayBetweenAccesses = config.getPropertyManager()
				.getDelayBetweenAccesses();
		nextTimeTarget = 0;
		currentFileItem = item;
	}

	private void sleepInterval() {
		long ms = nextTimeTarget - System.currentTimeMillis();
		if (ms < 0)
			return;
		sleepMs(ms);
	}

	@Override
	public void runner() throws Exception {

		PropertyManager propertyManager = config.getPropertyManager();
		String userAgent = propertyManager.getUserAgent();
		boolean dryRun = propertyManager.isDryRun();
		FileCrawlQueue crawlQueue = crawlMaster.getCrawlQueue();

		if (isAbort() || crawlMaster.isAbort())
			return;

		CrawlFile crawl = crawlFile(userAgent, dryRun);
		if (crawl != null) {
			if (!dryRun) {
				crawlQueue.add(crawl);
				if (crawlQueue.shouldWePersist())
					crawlQueue.index(false);
			}

			currentStats.incPendingUpdatedCount();
		}

		config.getFileManager().inject(currentFileItem);

		setStatus(CrawlStatus.INDEXATION);
		if (!dryRun)
			crawlMaster.getCrawlQueue().index(false);
	}

	private CrawlFile crawlFile(String userAgent, boolean dryRun)
			throws SearchLibException {

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		CrawlFile crawl = new CrawlFile(currentFileItem, config, currentStats);

		// Fetch started
		currentStats.incFetchedCount();
		if (dryRun)
			return crawl;

		sleepInterval();
		setStatus(CrawlStatus.CRAWL);

		crawl.download(currentFileItem);

		nextTimeTarget = System.currentTimeMillis() + delayBetweenAccesses;

		if (currentFileItem.getFetchStatus() == FetchStatus.FETCHED
				&& currentFileItem.getParserStatus() == ParserStatus.PARSED
				&& currentFileItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
			currentFileItem.setIndexStatus(IndexStatus.INDEXED);
			currentStats.incParsedCount();
		} else
			currentStats.incIgnoredCount();

		return crawl;
	}

	@Override
	public void abort() {
		super.abort();
	}

	public boolean getCrawlTimeOutExhausted(int seconds) {
		synchronized (this) {
			if (getStatus() != CrawlStatus.CRAWL)
				return false;
			return getStatusTimeElapsed() > seconds;
		}
	}

	public FileItem getCurrentFileItem() {
		synchronized (this) {
			return currentFileItem;
		}
	}

	public void setCurrentFileItem(FileItem item) {
		synchronized (this) {
			currentFileItem = item;
		}
	}

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
	}

	@Override
	public void complete() {
		crawlMaster.remove(this);
		synchronized (crawlMaster) {
			crawlMaster.notify();
		}

	}

	public String getDebugInfo() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(getThreadStatus());
			sb.append(' ');
			if (currentFileItem != null)
				sb.append(currentFileItem.getPath());
			return sb.toString();
		}
	}

}