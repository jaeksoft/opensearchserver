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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;

public class FileCrawlQueue extends CrawlQueueAbstract<CrawlFile, FileItem> {

	private List<CrawlFile> updateCrawlList;
	private List<FileItem> insertFileList;
	private List<String> deleteFileList;

	public FileCrawlQueue(Config config) throws SearchLibException {
		setConfig(config);
		this.updateCrawlList = new ArrayList<CrawlFile>(0);
		this.insertFileList = new ArrayList<FileItem>(0);
		this.deleteFileList = new ArrayList<String>(0);
	}

	public void add(CrawlFile crawl) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		synchronized (updateCrawlList) {
			updateCrawlList.add(crawl);
		}
	}

	public void delete(String url) {
		synchronized (deleteFileList) {
			deleteFileList.add(url);
			getSessionStats().incPendingDeletedCount();
		}
	}

	private boolean shouldWePersist() {
		synchronized (updateCrawlList) {
			if (updateCrawlList.size() > getMaxBufferSize())
				return true;
		}
		synchronized (deleteFileList) {
			if (deleteFileList.size() > getMaxBufferSize())
				return true;
		}
		synchronized (insertFileList) {
			if (insertFileList.size() > getMaxBufferSize())
				return true;
		}
		return false;
	}

	final private Object indexSync = new Object();

	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		List<CrawlFile> workUpdateCrawlList;
		List<FileItem> workInsertUrlList;
		List<String> workDeleteUrlList;
		synchronized (this) {
			if (!bForce)
				if (!shouldWePersist())
					return;
			workUpdateCrawlList = updateCrawlList;
			workInsertUrlList = insertFileList;
			insertFileList = new ArrayList<FileItem>(0);
			workDeleteUrlList = deleteFileList;
			deleteFileList = new ArrayList<String>(0);
		}

		FileManager urlManager = getConfig().getFileManager();
		// Synchronization to avoid simoultaneous indexation process
		synchronized (indexSync) {
			boolean needReload = false;
			if (deleteCollection(workDeleteUrlList))
				needReload = true;
			if (updateCrawls(workUpdateCrawlList))
				needReload = true;
			if (insertCollection(workInsertUrlList))
				needReload = true;
			if (needReload)
				urlManager.reload(false);
		}
	}

	protected boolean deleteCollection(List<String> workDeleteUrlList)
			throws SearchLibException {
		if (workDeleteUrlList.size() == 0)
			return false;
		getConfig().getFileManager().deleteFiles(workDeleteUrlList);
		getSessionStats().addDeletedCount(workDeleteUrlList.size());
		return true;
	}

	protected boolean updateCrawls(List<CrawlFile> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;
		getConfig().getFileManager().updateCrawls(workUpdateCrawlList);
		getSessionStats().addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	protected boolean insertCollection(List<FileItem> workInsertUrlList)
			throws SearchLibException {
		if (workInsertUrlList.size() == 0)
			return false;
		getConfig().getFileManager().updateFileItems(workInsertUrlList);
		getSessionStats().addNewUrlCount(workInsertUrlList.size());
		return true;
	}
}
