/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.crawler.web.process;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;

public class CrawlQueue {

	private Config config;

	private CrawlStatistics sessionStats;

	private List<Crawl> updateCrawlList;

	private List<UrlItem> insertUrlList;

	private List<String> deleteUrlList;

	private int maxBufferSize;

	protected CrawlQueue(Config config) throws SearchLibException {
		this.config = config;
		this.sessionStats = null;
		this.updateCrawlList = new ArrayList<Crawl>(0);
		this.insertUrlList = new ArrayList<UrlItem>(0);
		this.deleteUrlList = new ArrayList<String>(0);
		this.maxBufferSize = config.getPropertyManager()
				.getIndexDocumentBufferSize();
	}

	protected void add(Crawl crawl) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		synchronized (updateCrawlList) {
			updateCrawlList.add(crawl);
		}
		List<String> discoverLinks = crawl.getDiscoverLinks();
		synchronized (insertUrlList) {
			if (discoverLinks != null)
				for (String link : discoverLinks)
					insertUrlList.add(new UrlItem(link));
		}
	}

	public void delete(String url) {
		synchronized (deleteUrlList) {
			deleteUrlList.add(url);
			sessionStats.incPendingDeletedCount();
		}
	}

	private boolean shouldWePersist() {
		synchronized (updateCrawlList) {
			if (updateCrawlList.size() > maxBufferSize)
				return true;
		}
		synchronized (deleteUrlList) {
			if (deleteUrlList.size() > maxBufferSize)
				return true;
		}
		synchronized (insertUrlList) {
			if (insertUrlList.size() > maxBufferSize)
				return true;
		}
		return false;
	}

	final private Object indexSync = new Object();

	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		List<Crawl> workUpdateCrawlList;
		List<UrlItem> workInsertUrlList;
		List<String> workDeleteUrlList;
		synchronized (this) {
			if (!bForce)
				if (!shouldWePersist())
					return;
			workUpdateCrawlList = updateCrawlList;
			updateCrawlList = new ArrayList<Crawl>(0);
			workInsertUrlList = insertUrlList;
			insertUrlList = new ArrayList<UrlItem>(0);
			workDeleteUrlList = deleteUrlList;
			deleteUrlList = new ArrayList<String>(0);
		}

		UrlManager urlManager = config.getUrlManager();
		// Synchronization to avoid simoultaneous indexation process
		synchronized (indexSync) {
			boolean needReload = false;
			if (deleteUrls(workDeleteUrlList))
				needReload = true;
			if (updateCrawls(workUpdateCrawlList))
				needReload = true;
			if (insertUrls(workInsertUrlList))
				needReload = true;
			if (needReload)
				urlManager.reload(false);
		}
	}

	private boolean deleteUrls(List<String> workDeleteUrlList)
			throws SearchLibException {
		if (workDeleteUrlList.size() == 0)
			return false;
		UrlManager urlManager = config.getUrlManager();
		urlManager.deleteUrls(workDeleteUrlList);
		sessionStats.addDeletedCount(workDeleteUrlList.size());
		return true;
	}

	private boolean updateCrawls(List<Crawl> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;
		UrlManager urlManager = config.getUrlManager();
		urlManager.updateCrawls(workUpdateCrawlList);
		sessionStats.addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	private boolean insertUrls(List<UrlItem> workInsertUrlList)
			throws SearchLibException {
		if (workInsertUrlList.size() == 0)
			return false;
		UrlManager urlManager = config.getUrlManager();
		urlManager.updateUrlItems(workInsertUrlList);
		sessionStats.addNewUrlCount(workInsertUrlList.size());
		return true;
	}

	public void setStatistiques(CrawlStatistics stats) {
		this.sessionStats = stats;
	}

}
