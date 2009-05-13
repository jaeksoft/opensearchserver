/**   
 * License Agreement for Jaeksoft WebSearch
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft WebSearch.
 *
 * Jaeksoft WebSearch is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft WebSearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft WebSearch. 
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
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.index.IndexDocument;

public class CrawlQueue {

	private Config config;

	private CrawlStatistics sessionStats;

	final private static Logger logger = Logger.getLogger(CrawlQueue.class
			.getCanonicalName());

	private List<IndexDocument> updateUrlList;

	private List<IndexDocument> newUrlList;

	private List<String> deleteUrlList;

	private int maxBufferSize;

	protected CrawlQueue(Config config) throws SearchLibException {
		this.config = config;
		this.sessionStats = null;
		this.updateUrlList = new ArrayList<IndexDocument>();
		this.newUrlList = new ArrayList<IndexDocument>();
		this.deleteUrlList = new ArrayList<String>();
		this.maxBufferSize = config.getPropertyManager()
				.getIndexDocumentBufferSize();
	}

	protected void add(Crawl crawl) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		List<IndexDocument> discoverLinks = crawl.getDiscoverLinks();
		synchronized (updateUrlList) {
			updateUrlList.add(crawl.getIndexDocument());
		}
		synchronized (newUrlList) {
			if (discoverLinks != null)
				for (IndexDocument idxDoc : discoverLinks)
					newUrlList.add(idxDoc);
		}
	}

	public void delete(String url) {
		synchronized (deleteUrlList) {
			deleteUrlList.add(url);
			sessionStats.incPendingDeletedCount();
		}
	}

	private boolean shouldWePersist() {
		synchronized (updateUrlList) {
			if (updateUrlList.size() > maxBufferSize)
				return true;
		}
		synchronized (deleteUrlList) {
			if (deleteUrlList.size() > maxBufferSize)
				return true;
		}
		synchronized (newUrlList) {
			if (newUrlList.size() > maxBufferSize)
				return true;
		}
		return false;
	}

	final private Object indexSync = new Object();

	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException {
		List<IndexDocument> workUpdateUrlList;
		List<IndexDocument> workNewUrlList;
		List<String> workDeleteUrlList;
		synchronized (this) {
			if (!bForce)
				if (!shouldWePersist())
					return;
			synchronized (updateUrlList) {
				workUpdateUrlList = updateUrlList;
				updateUrlList = new ArrayList<IndexDocument>();
			}
			synchronized (newUrlList) {
				workNewUrlList = newUrlList;
				newUrlList = new ArrayList<IndexDocument>();
			}
			synchronized (deleteUrlList) {
				workDeleteUrlList = deleteUrlList;
				deleteUrlList = new ArrayList<String>();
			}
		}

		if (logger.isLoggable(Level.INFO))
			logger.info("Real indexation starts " + workUpdateUrlList.size()
					+ "/" + workNewUrlList.size() + "/"
					+ workDeleteUrlList.size());
		UrlManager urlManager = config.getUrlManager();
		synchronized (indexSync) {
			boolean needReload = false;
			if (workDeleteUrlList.size() > 0) {
				urlManager.deleteUrls(workDeleteUrlList);
				if (logger.isLoggable(Level.INFO))
					logger.info("Deleting " + workDeleteUrlList.size()
							+ " document(s)");
				sessionStats.addDeletedCount(workDeleteUrlList.size());
				needReload = true;
			}
			if (workUpdateUrlList.size() > 0) {
				if (logger.isLoggable(Level.INFO))
					logger.info("Update " + workUpdateUrlList.size()
							+ " document(s)");
				urlManager.updateDocuments(workUpdateUrlList);
				sessionStats.addUpdatedCount(workUpdateUrlList.size());
				needReload = true;
			}
			if (workNewUrlList.size() > 0) {
				if (logger.isLoggable(Level.INFO))
					logger.info("Update " + workNewUrlList.size()
							+ " document(s)");
				urlManager.updateDocuments(workNewUrlList);
				sessionStats.addNewUrlCount(workNewUrlList.size());
				needReload = true;
			}
			if (needReload)
				urlManager.reload(false);
		}
	}

	public void setStatistiques(CrawlStatistics stats) {
		this.sessionStats = stats;
	}

}
