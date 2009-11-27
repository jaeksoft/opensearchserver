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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;

public class UrlCrawlQueue extends CrawlQueueAbstract<Crawl, UrlItem> {

	private List<Crawl> updateCrawlList;
	private List<UrlItem> insertUrlList;
	private List<String> deleteUrlList;

	private List<Crawl> workingUpdateCrawlList;
	private List<UrlItem> workingInsertUrlList;
	private List<String> workingDeleteUrlList;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public UrlCrawlQueue(Config config) throws SearchLibException {
		setConfig(config);
		updateCrawlList = new ArrayList<Crawl>(0);
		insertUrlList = new ArrayList<UrlItem>(0);
		deleteUrlList = new ArrayList<String>(0);
		workingUpdateCrawlList = null;
		workingInsertUrlList = null;
		workingDeleteUrlList = null;
	}

	@Override
	public void add(Crawl crawl) throws NoSuchAlgorithmException, IOException,
			SearchLibException {
		r.lock();
		try {
			updateCrawlList.add(crawl);
			List<String> discoverLinks = crawl.getDiscoverLinks();
			if (discoverLinks != null) {
				getSessionStats().addPendingNewUrlCount(discoverLinks.size());
				for (String link : discoverLinks)
					insertUrlList.add(new UrlItem(link));
			}
		} finally {
			r.unlock();
		}
	}

	@Override
	public void delete(String url) {
		r.lock();
		try {
			deleteUrlList.add(url);
			getSessionStats().incPendingDeletedCount();
		} finally {
			r.unlock();
		}
	}

	private boolean shouldWePersist() {
		if (updateCrawlList.size() > getMaxBufferSize())
			return true;
		if (deleteUrlList.size() > getMaxBufferSize())
			return true;
		if (insertUrlList.size() > getMaxBufferSize())
			return true;
		return false;
	}

	final private Object indexSync = new Object();

	@Override
	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		w.lock();
		try {
			if (!bForce)
				if (!shouldWePersist())
					return;
			if (workingUpdateCrawlList != null)
				return;
			if (workingInsertUrlList != null)
				return;
			if (workingDeleteUrlList != null)
				return;
			workingUpdateCrawlList = updateCrawlList;
			workingInsertUrlList = insertUrlList;
			workingDeleteUrlList = deleteUrlList;

			updateCrawlList = new ArrayList<Crawl>(0);
			insertUrlList = new ArrayList<UrlItem>(0);
			deleteUrlList = new ArrayList<String>(0);
		} finally {
			w.unlock();
		}

		UrlManager urlManager = getConfig().getUrlManager();
		boolean needReload = false;
		if (deleteCollection(workingDeleteUrlList))
			needReload = true;
		if (updateCrawls(workingUpdateCrawlList))
			needReload = true;
		if (insertCollection(workingInsertUrlList))
			needReload = true;
		if (needReload)
			urlManager.reload(false);

		w.lock();
		try {
			workingUpdateCrawlList = null;
			workingInsertUrlList = null;
			workingDeleteUrlList = null;
		} finally {
			w.unlock();
		}
	}

	@Override
	protected boolean deleteCollection(List<String> workDeleteUrlList)
			throws SearchLibException {
		if (workDeleteUrlList.size() == 0)
			return false;
		UrlManager urlManager = getConfig().getUrlManager();
		urlManager.deleteUrls(workDeleteUrlList);
		getSessionStats().addDeletedCount(workDeleteUrlList.size());
		return true;
	}

	@Override
	protected boolean updateCrawls(List<Crawl> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;
		UrlManager urlManager = (UrlManager) getConfig().getUrlManager();
		urlManager.updateCrawls(workUpdateCrawlList);
		getSessionStats().addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	@Override
	protected boolean insertCollection(List<UrlItem> workInsertUrlList)
			throws SearchLibException {
		if (workInsertUrlList.size() == 0)
			return false;
		UrlManager urlManager = getConfig().getUrlManager();
		urlManager.updateUrlItems(workInsertUrlList);
		getSessionStats().addNewUrlCount(workInsertUrlList.size());
		return true;
	}
}
