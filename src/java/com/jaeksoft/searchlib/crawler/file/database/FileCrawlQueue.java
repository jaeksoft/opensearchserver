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
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.file.spider.CrawlFile;

public class FileCrawlQueue extends CrawlQueueAbstract<CrawlFile, FileItem> {

	private List<CrawlFile> updateCrawlList;
	private List<String> deleteUriList;
	private List<String> deleteOriginalUriList;
	final private Object indexSync = new Object();

	public FileCrawlQueue(Config config) throws SearchLibException {
		setConfig(config);
		this.updateCrawlList = new ArrayList<CrawlFile>(0);
		this.deleteUriList = new ArrayList<String>(0);
		this.deleteOriginalUriList = new ArrayList<String>(0);
	}

	@Override
	public void add(CrawlFile crawl) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		synchronized (updateCrawlList) {
			updateCrawlList.add(crawl);
		}
	}

	@Override
	public void delete(String uri) {
		synchronized (deleteUriList) {
			deleteUriList.add(uri);
		}
	}

	public void deleteByOriginalUri(String originalUri) {
		synchronized (deleteOriginalUriList) {
			deleteOriginalUriList.add(originalUri);
		}
	}

	public boolean shouldWePersist() {
		synchronized (updateCrawlList) {
			if (updateCrawlList.size() >= getMaxBufferSize())
				return true;
			if (deleteUriList.size() >= getMaxBufferSize())
				return true;
			if (deleteOriginalUriList.size() >= 1)
				return true;
		}

		return false;
	}

	@Override
	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		List<CrawlFile> workUpdateCrawlList;
		List<String> workDeleteUriList;
		List<String> workDeleteOriginalUriList;
		synchronized (this) {
			if (!bForce)
				if (!shouldWePersist())
					return;

			workUpdateCrawlList = updateCrawlList;
			updateCrawlList = new ArrayList<CrawlFile>(0);

			workDeleteUriList = deleteUriList;
			deleteUriList = new ArrayList<String>(0);

			workDeleteOriginalUriList = deleteOriginalUriList;
			deleteOriginalUriList = new ArrayList<String>(0);
		}

		FileManager fileManager = getConfig().getFileManager();

		// Synchronization to avoid simoultaneous indexation process
		synchronized (indexSync) {
			boolean needReload = false;

			// Update
			if (updateCrawls(workUpdateCrawlList))
				needReload = true;

			// Delete
			if (deleteCollection(workDeleteUriList))
				needReload = true;

			// Delete by original URI
			if (deleteCollectionByOriginalURI(workDeleteOriginalUriList))
				needReload = true;

			if (needReload)
				fileManager.reload(false);
		}
	}

	@Override
	protected boolean insertCollection(List<FileItem> workInsertUrlList)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean updateCrawls(List<CrawlFile> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;

		FileManager manager = (FileManager) getConfig().getFileManager();
		manager.updateCrawls(workUpdateCrawlList);
		getSessionStats().addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	@Override
	protected boolean deleteCollection(List<String> workDeleteUriList)
			throws SearchLibException {
		if (workDeleteUriList.size() == 0)
			return false;

		FileManager manager = (FileManager) getConfig().getFileManager();
		int nbFilesDeleted = manager.deleteByFilename(workDeleteUriList);
		getSessionStats().addDeletedCount(nbFilesDeleted);
		return true;
	}

	protected boolean deleteCollectionByOriginalURI(
			List<String> workDeleteOriginalUriList) throws SearchLibException {
		if (workDeleteOriginalUriList.size() == 0)
			return false;

		FileManager manager = (FileManager) getConfig().getFileManager();
		int nbFilesDeleted = manager
				.deleteByOriginalUri(workDeleteOriginalUriList);
		getSessionStats().addDeletedCount(nbFilesDeleted);
		return true;
	}
}
