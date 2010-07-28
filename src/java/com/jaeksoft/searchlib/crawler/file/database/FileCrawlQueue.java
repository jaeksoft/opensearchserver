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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class FileCrawlQueue extends CrawlQueueAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private List<CrawlFile> updateCrawlList;
	private List<String> deleteUriList;

	private List<CrawlFile> workingUpdateCrawlList;
	private List<String> workingDeleteUriList;

	public FileCrawlQueue(Config config, PropertyManager propertyManager)
			throws SearchLibException {
		super(config, propertyManager.getIndexDocumentBufferSize().getValue());
		this.updateCrawlList = new ArrayList<CrawlFile>(0);
		this.deleteUriList = new ArrayList<String>(0);
	}

	public void add(CrawlStatistics crawlStats, CrawlFile crawl)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
		rwl.r.lock();
		try {
			updateCrawlList.add(crawl);
		} finally {
			rwl.r.unlock();
		}
	}

	public void delete(CrawlStatistics crawlStats, String uri) {
		rwl.r.lock();
		try {
			deleteUriList.add(uri);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected boolean shouldWePersist() {
		rwl.r.lock();
		try {
			if (updateCrawlList.size() >= getMaxBufferSize())
				return true;
			if (deleteUriList.size() >= getMaxBufferSize())
				return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected boolean workingInProgress() {
		rwl.r.lock();
		try {
			if (workingUpdateCrawlList != null)
				return true;
			if (workingDeleteUriList != null)
				return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void initWorking() {
		rwl.w.lock();
		try {
			workingUpdateCrawlList = updateCrawlList;
			workingDeleteUriList = deleteUriList;

			updateCrawlList = new ArrayList<CrawlFile>(0);
			deleteUriList = new ArrayList<String>(0);

			getSessionStats().resetPending();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	protected void resetWork() {
		rwl.w.lock();
		try {
			workingUpdateCrawlList = null;
			workingDeleteUriList = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	protected void indexWork() throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		FileManager fileManager = getConfig().getFileManager();
		boolean needReload = false;
		if (deleteCollection(workingDeleteUriList))
			needReload = true;
		if (updateCrawls(workingUpdateCrawlList))
			needReload = true;
		if (needReload)
			fileManager.reload(false);
	}

	protected boolean updateCrawls(List<CrawlFile> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;

		FileManager manager = (FileManager) getConfig().getFileManager();
		manager.updateCrawls(workUpdateCrawlList);
		getSessionStats().addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	protected boolean deleteCollection(List<String> workDeleteUriList)
			throws SearchLibException {
		if (workDeleteUriList.size() == 0)
			return false;

		FileManager manager = (FileManager) getConfig().getFileManager();
		int nbFilesDeleted = manager.deleteByFilename(workDeleteUriList) ? 1
				: 0;
		getSessionStats().addDeletedCount(nbFilesDeleted);
		return true;
	}

}
