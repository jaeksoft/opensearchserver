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

package com.jaeksoft.searchlib.crawler.web.database;

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
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class UrlCrawlQueue extends CrawlQueueAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private List<Crawl> updateCrawlList;
	private List<UrlItem> insertUrlList;
	private List<String> deleteUrlList;

	private List<Crawl> workingUpdateCrawlList;
	private List<UrlItem> workingInsertUrlList;
	private List<String> workingDeleteUrlList;

	public UrlCrawlQueue(Config config, WebPropertyManager propertyManager)
			throws SearchLibException {
		super(config, propertyManager.getIndexDocumentBufferSize().getValue());
		updateCrawlList = new ArrayList<Crawl>(0);
		insertUrlList = new ArrayList<UrlItem>(0);
		deleteUrlList = new ArrayList<String>(0);
		workingUpdateCrawlList = null;
		workingInsertUrlList = null;
		workingDeleteUrlList = null;
	}

	public void add(CrawlStatistics currentStats, Crawl crawl)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
		rwl.r.lock();
		try {
			updateCrawlList.add(crawl);
			currentStats.incPendingUpdateCount();
			List<LinkItem> discoverLinks = crawl.getDiscoverLinks();
			UrlManager urlManager = getConfig().getUrlManager();
			if (discoverLinks != null) {
				for (LinkItem link : discoverLinks)
					insertUrlList.add(urlManager.getNewUrlItem(link));
				currentStats.addPendingNewUrlCount(discoverLinks.size());
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public void delete(CrawlStatistics currentStats, String url) {
		rwl.r.lock();
		try {
			if (url == null)
				return;
			deleteUrlList.add(url);
			currentStats.incPendingDeleteCount();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected boolean shouldWePersist() {
		rwl.r.lock();
		try {
			if (updateCrawlList.size() > getMaxBufferSize())
				return true;
			if (deleteUrlList.size() > getMaxBufferSize() * 10)
				return true;
			if (insertUrlList.size() > getMaxBufferSize() * 10)
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
			if (workingInsertUrlList != null)
				return true;
			if (workingDeleteUrlList != null)
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
			workingInsertUrlList = insertUrlList;
			workingDeleteUrlList = deleteUrlList;

			updateCrawlList = new ArrayList<Crawl>(0);
			insertUrlList = new ArrayList<UrlItem>(0);
			deleteUrlList = new ArrayList<String>(0);

			if (getSessionStats() != null)
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
			workingInsertUrlList = null;
			workingDeleteUrlList = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	protected void indexWork() throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		UrlManager urlManager = getConfig().getUrlManager();
		boolean needReload = false;
		CrawlStatistics sessionStats = getSessionStats();
		if (deleteCollection(workingDeleteUrlList, sessionStats))
			needReload = true;
		if (updateCrawls(workingUpdateCrawlList, sessionStats))
			needReload = true;
		if (insertCollection(workingInsertUrlList, sessionStats))
			needReload = true;
		if (needReload)
			urlManager.reload(false, null);
	}

	private boolean deleteCollection(List<String> workDeleteUrlList,
			CrawlStatistics sessionStats) throws SearchLibException {
		if (workDeleteUrlList.size() == 0)
			return false;
		UrlManager urlManager = getConfig().getUrlManager();
		urlManager.deleteUrls(workDeleteUrlList);
		if (sessionStats != null)
			sessionStats.addDeletedCount(workDeleteUrlList.size());
		return true;
	}

	private boolean updateCrawls(List<Crawl> workUpdateCrawlList,
			CrawlStatistics sessionStats) throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;
		UrlManager urlManager = getConfig().getUrlManager();
		urlManager.updateCrawlTarget(workUpdateCrawlList);
		urlManager.updateCrawlUrlDb(workUpdateCrawlList);
		if (sessionStats != null)
			sessionStats.addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	private boolean insertCollection(List<UrlItem> workInsertUrlList,
			CrawlStatistics sessionStats) throws SearchLibException {
		if (workInsertUrlList.size() == 0)
			return false;
		UrlManager urlManager = getConfig().getUrlManager();
		urlManager.updateUrlItems(workInsertUrlList);
		if (sessionStats != null)
			sessionStats.addNewUrlCount(workInsertUrlList.size());
		return true;
	}
}
