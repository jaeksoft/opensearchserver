/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;

public class CrawlQueue {

	private Config config;

	private CrawlStatistics sessionStats;

	final private static Logger logger = Logger.getLogger(CrawlQueue.class
			.getCanonicalName());

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
			synchronized (updateCrawlList) {
				workUpdateCrawlList = updateCrawlList;
				updateCrawlList = new ArrayList<Crawl>(0);
			}
			synchronized (insertUrlList) {
				workInsertUrlList = insertUrlList;
				insertUrlList = new ArrayList<UrlItem>(0);
			}
			synchronized (deleteUrlList) {
				workDeleteUrlList = deleteUrlList;
				deleteUrlList = new ArrayList<String>(0);
			}
		}

		if (logger.isLoggable(Level.INFO))
			logger.info("Real indexation starts " + workUpdateCrawlList.size()
					+ "/" + workInsertUrlList.size() + "/"
					+ workDeleteUrlList.size());
		UrlManager urlManager = config.getUrlManager();
		// Synchronization to avoid simoultaneous indexation process
		synchronized (indexSync) {
			boolean needReload = deleteUrls(workDeleteUrlList);
			needReload = needReload || updateCrawls(workUpdateCrawlList);
			needReload = needReload || insertUrls(workInsertUrlList);
			if (needReload)
				urlManager.reload(false);
		}
	}

	private boolean deleteUrls(List<String> workDeleteUrlList)
			throws SearchLibException {
		if (workDeleteUrlList.size() == 0)
			return false;
		if (logger.isLoggable(Level.INFO))
			logger
					.info("Deleting " + workDeleteUrlList.size()
							+ " document(s)");
		UrlManager urlManager = config.getUrlManager();
		urlManager.deleteUrls(workDeleteUrlList);
		sessionStats.addDeletedCount(workDeleteUrlList.size());
		return true;
	}

	private boolean updateCrawls(List<Crawl> workUpdateCrawlList)
			throws SearchLibException {
		if (workUpdateCrawlList.size() == 0)
			return false;
		if (logger.isLoggable(Level.INFO))
			logger
					.info("Update " + workUpdateCrawlList.size()
							+ " document(s)");
		UrlManager urlManager = config.getUrlManager();
		urlManager.updateCrawls(workUpdateCrawlList);
		sessionStats.addUpdatedCount(workUpdateCrawlList.size());
		return true;
	}

	private boolean insertUrls(List<UrlItem> workInsertUrlList)
			throws SearchLibException {
		if (workInsertUrlList.size() == 0)
			return false;
		if (logger.isLoggable(Level.INFO))
			logger.info("Insert " + workInsertUrlList.size() + " document(s)");
		UrlManager urlManager = config.getUrlManager();
		urlManager.updateUrlItems(workInsertUrlList);
		sessionStats.addNewUrlCount(workInsertUrlList.size());
		return true;
	}

	public void setStatistiques(CrawlStatistics stats) {
		this.sessionStats = stats;
	}

}
