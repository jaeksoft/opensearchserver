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
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;

public class CrawlFileThread extends CrawlThreadAbstract {

	private FileItem currentFileItem;

	protected CrawlFileThread(Config config, CrawlFileMaster crawlMaster,
			CrawlStatistics sessionStats) throws SearchLibException {
		super(config, crawlMaster);
		currentStats = new CrawlStatistics(sessionStats);
		currentFileItem = null;
	}

	@Override
	public void runner() throws Exception {
		FilePropertyManager propertyManager = config.getFilePropertyManager();
		boolean dryRun = propertyManager.getDryRun().getValue();

		CrawlFileMaster crawlMaster = (CrawlFileMaster) getCrawlMaster();
		FileCrawlQueue crawlQueue = (FileCrawlQueue) crawlMaster
				.getCrawlQueue();

		currentStats.addListSize(1);

		File file;

		while ((file = crawlMaster.getNextFile()) != null) {

			if (isAbort() || crawlMaster.isAbort())
				return;

			currentFileItem = new FileItem(file);

			CrawlFile crawl = crawl(dryRun);
			if (crawl != null)
				if (!dryRun)
					crawlQueue.add(currentStats, crawl);
		}

		setStatus(CrawlStatus.INDEXATION);
		if (!dryRun)
			crawlQueue.index(false);
	}

	private CrawlFile crawl(boolean dryRun) throws SearchLibException {

		setStatus(CrawlStatus.CRAWL);
		currentStats.incUrlCount();

		CrawlFile crawl = new CrawlFile(currentFileItem, config, currentStats);

		// Fetch started
		currentStats.incFetchedCount();
		if (dryRun)
			return crawl;

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

	@Override
	public void release() {
	}

}