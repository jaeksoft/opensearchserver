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

package com.jaeksoft.searchlib.crawler.common.process;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.PropertyManager;

public abstract class CrawlQueueAbstract<T, Z> {

	private Config config;
	private CrawlStatistics sessionStats;
	private int maxBufferSize;

	public CrawlQueueAbstract() {
	}

	public CrawlQueueAbstract(Config config, PropertyManager propertyManager)
			throws SearchLibException {
		this.config = config;
		this.setSessionStats(null);
		this.setMaxBufferSize(propertyManager.getIndexDocumentBufferSize()
				.getValue());
	}

	public abstract void index(boolean bForce) throws SearchLibException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException;

	public abstract void add(CrawlStatistics currentStats, T crawl)
			throws NoSuchAlgorithmException, IOException, SearchLibException;

	public abstract void delete(CrawlStatistics currentStats, String url);

	protected abstract boolean deleteCollection(List<String> workDeleteUrlList)
			throws SearchLibException;

	protected abstract boolean insertCollection(List<Z> workInsertUrlList)
			throws SearchLibException;

	protected abstract boolean updateCrawls(List<T> workUpdateCrawlList)
			throws SearchLibException;

	public void setStatistiques(CrawlStatistics stats) {
		this.sessionStats = stats;
	}

	public CrawlStatistics getSessionStats() {
		return sessionStats;
	}

	public void setSessionStats(CrawlStatistics sessionStats) {
		this.sessionStats = sessionStats;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
