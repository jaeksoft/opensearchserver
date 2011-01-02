/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FileInfo;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;

public class CrawlFileThread extends CrawlThreadAbstract {

	private FileItem currentFileItem;
	private FileManager fileManager;
	private long delayBetweenAccesses;
	private FilePathItem filePathItem;
	private long nextTimeTarget;

	protected CrawlFileThread(Config config, CrawlFileMaster crawlMaster,
			CrawlStatistics sessionStats, FilePathItem filePathItem)
			throws SearchLibException {
		super(config, crawlMaster);
		this.fileManager = config.getFileManager();
		currentStats = new CrawlStatistics(sessionStats);
		delayBetweenAccesses = filePathItem.getDelay();
		nextTimeTarget = 0;
		this.filePathItem = filePathItem;
	}

	private void sleepInterval() {
		long c = System.currentTimeMillis();
		long ms = nextTimeTarget - c;
		nextTimeTarget = c + delayBetweenAccesses;
		if (ms < 0)
			return;
		sleepMs(ms);
	}

	@Override
	public void runner() throws Exception {

		CrawlFileMaster crawlMaster = (CrawlFileMaster) getThreadMaster();
		FileCrawlQueue crawlQueue = (FileCrawlQueue) crawlMaster
				.getCrawlQueue();

		FilePathItemIterator filePathIterator = new FilePathItemIterator(
				filePathItem);

		FileInstanceAbstract fileInstance;

		while ((fileInstance = filePathIterator.next()) != null) {

			if (isAborted() || crawlMaster.isAborted())
				break;

			currentFileItem = new FileItem(fileInstance);

			FileTypeEnum type = currentFileItem.getType();
			if (type == FileTypeEnum.directory) {
				checkDirectory();
			} else if (type == FileTypeEnum.file) {
				if (!checkFile())
					continue;
			}

			CrawlFile crawl = crawl();
			if (crawl != null)
				crawlQueue.add(currentStats, crawl);

			setStatus(CrawlStatus.INDEXATION);
			crawlQueue.index(false);

		}
		crawlQueue.index(!crawlMaster.isRunning());
	}

	private CrawlFile crawl() throws SearchLibException {

		sleepInterval();

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		CrawlFile crawl = new CrawlFile(currentFileItem, getConfig(),
				currentStats);

		// Fetch started
		currentStats.incFetchedCount();

		crawl.download();

		if (currentFileItem.getFetchStatus() == FetchStatus.FETCHED
				&& currentFileItem.getParserStatus() == ParserStatus.PARSED
				&& currentFileItem.getIndexStatus() != IndexStatus.META_NOINDEX) {
			crawl.getFileItem().setIndexStatus(IndexStatus.INDEXED);
			currentFileItem.setIndexStatus(IndexStatus.INDEXED);
			currentStats.incParsedCount();
		} else
			currentStats.incIgnoredCount();

		return crawl;
	}

	private void checkDirectory() {
		// Load directory from Index

		// Compare to current file list
	}

	private boolean checkFile() throws UnsupportedEncodingException,
			SearchLibException, URISyntaxException {
		FileInfo oldFileInfo = fileManager
				.getFileInfo(currentFileItem.getURI());
		// The file is a new file
		if (oldFileInfo == null)
			return true;
		// The file has been modified
		if (oldFileInfo.isNewCrawlNeeded(currentFileItem))
			return true;
		// The file has not changed, we don't need to craw it
		currentStats.incIgnoredCount();
		return false;
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

	@Override
	public String getCurrentInfo() {
		if (currentFileItem != null)
			return currentFileItem.getURI().toASCIIString();
		return "";
	}

}