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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class FileCrawlQueue extends CrawlQueueAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private List<CrawlFile> updateCrawlList;
	private List<String> deleteUriList;
	private List<String> deleteParentUriList;

	private List<CrawlFile> workingUpdateCrawlList;
	private List<String> workingDeleteUriList;
	private List<String> workingDeleteParentUriList;

	public FileCrawlQueue(Config config, FilePropertyManager propertyManager)
			throws SearchLibException {
		super(config, propertyManager.getIndexDocumentBufferSize().getValue());
		this.updateCrawlList = new ArrayList<CrawlFile>(0);
		this.deleteUriList = new ArrayList<String>(0);
		this.deleteParentUriList = new ArrayList<String>(0);
	}

	public void add(CrawlStatistics crawlStats, CrawlFile crawl)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
		rwl.r.lock();
		try {
			updateCrawlList.add(crawl);
			crawlStats.incPendingUpdateCount();
		} finally {
			rwl.r.unlock();
		}
	}

	public void delete(CrawlStatistics crawlStats, String uri) {
		rwl.r.lock();
		try {
			deleteUriList.add(uri);
			crawlStats.incPendingDeleteCount();
		} finally {
			rwl.r.unlock();
		}
	}

	public void deleteParent(CrawlStatistics crawlStats, String parentUri) {
		rwl.r.lock();
		try {
			deleteParentUriList.add(parentUri);
			crawlStats.incPendingDeleteCount();
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
			if (deleteParentUriList.size() >= getMaxBufferSize())
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
			if (workingDeleteParentUriList != null)
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
			workingDeleteParentUriList = deleteParentUriList;

			updateCrawlList = new ArrayList<CrawlFile>(0);
			deleteUriList = new ArrayList<String>(0);
			deleteParentUriList = new ArrayList<String>(0);

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
			workingDeleteParentUriList = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	protected void indexWork() throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		FileManager fileManager = getConfig().getFileManager();
		CrawlStatistics sessionStats = getSessionStats();
		boolean needReload = false;
		if (deleteParentCollection(workingDeleteParentUriList, sessionStats))
			needReload = true;
		if (deleteCollection(workingDeleteUriList, sessionStats))
			needReload = true;
		if (updateCrawls(workingUpdateCrawlList, sessionStats))
			needReload = true;
		if (needReload)
			fileManager.reload(false);
	}

	protected boolean updateCrawls(List<CrawlFile> workUpdateCrawlList,
			CrawlStatistics sessionStats) throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;

		FileManager manager = getConfig().getFileManager();
		manager.updateCrawls(workUpdateCrawlList);
		if (sessionStats != null)
			sessionStats.addUpdatedCount(workUpdateCrawlList.size());
		setContainedData();
		return true;
	}

	protected boolean deleteCollection(List<String> workDeleteUriList,
			CrawlStatistics sessionStats) throws SearchLibException {
		if (workDeleteUriList.size() == 0)
			return false;

		FileManager manager = getConfig().getFileManager();
		int nbFilesDeleted = manager.deleteByUri(workDeleteUriList) ? 1 : 0;
		if (sessionStats != null)
			sessionStats.addDeletedCount(nbFilesDeleted);
		setContainedData();
		return true;
	}

	protected boolean deleteParentCollection(
			List<String> workDeleteParentUriList, CrawlStatistics sessionStats)
			throws SearchLibException {
		if (workDeleteParentUriList.size() == 0)
			return false;

		FileManager manager = getConfig().getFileManager();
		int nbFilesDeleted = manager.deleteByParentUri(workDeleteParentUriList) ? 1
				: 0;
		if (sessionStats != null)
			sessionStats.addDeletedCount(nbFilesDeleted);
		setContainedData();
		return true;
	}

}
