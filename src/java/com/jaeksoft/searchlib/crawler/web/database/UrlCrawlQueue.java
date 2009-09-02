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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;

public class UrlCrawlQueue extends CrawlQueueAbstract<Crawl, UrlItem> {

	private List<Crawl> updateCrawlList;
	private List<UrlItem> insertUrlList;
	private List<String> deleteUrlList;

	public UrlCrawlQueue(Config config) throws SearchLibException {
		setConfig(config);
		this.updateCrawlList = new ArrayList<Crawl>(0);
		this.insertUrlList = new ArrayList<UrlItem>(0);
		this.deleteUrlList = new ArrayList<String>(0);
	}

	@Override
	public void add(Crawl crawl) throws NoSuchAlgorithmException, IOException,
			SearchLibException {
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

	@Override
	public void delete(String url) {
		synchronized (deleteUrlList) {
			deleteUrlList.add(url);
			getSessionStats().incPendingDeletedCount();
		}
	}

	private boolean shouldWePersist() {
		synchronized (updateCrawlList) {
			if (updateCrawlList.size() > getMaxBufferSize())
				return true;
		}
		synchronized (deleteUrlList) {
			if (deleteUrlList.size() > getMaxBufferSize())
				return true;
		}
		synchronized (insertUrlList) {
			if (insertUrlList.size() > getMaxBufferSize())
				return true;
		}
		return false;
	}

	final private Object indexSync = new Object();

	@Override
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
			workInsertUrlList = insertUrlList;
			workDeleteUrlList = deleteUrlList;

			updateCrawlList = new ArrayList<Crawl>(0);
			insertUrlList = new ArrayList<UrlItem>(0);
			deleteUrlList = new ArrayList<String>(0);
		}

		UrlManager urlManager = getConfig().getUrlManager();
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
