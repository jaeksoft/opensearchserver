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

package com.jaeksoft.searchlib.crawler.common.process;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.SimpleLock;

public abstract class CrawlQueueAbstract {

	private Config config;
	private CrawlStatistics sessionStats;
	private int maxBufferSize;
	private boolean containedData;

	private final SimpleLock lock = new SimpleLock();

	protected CrawlQueueAbstract(Config config, int maxBufferSize)
			throws SearchLibException {
		this.sessionStats = null;
		this.config = config;
		this.maxBufferSize = maxBufferSize;
		this.containedData = false;
	}

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

	public Config getConfig() {
		return config;
	}

	protected abstract boolean workingInProgress();

	protected abstract boolean shouldWePersist();

	protected void setContainedData() {
		if (!this.containedData)
			this.containedData = true;
	}

	public boolean hasContainedData() {
		return containedData;
	}

	private boolean weMustIndexNow() {
		synchronized (this) {
			if (!shouldWePersist())
				return false;
			if (workingInProgress())
				return false;
			return true;
		}
	}

	protected abstract void indexWork() throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException;

	protected abstract void initWorking();

	protected abstract void resetWork();

	public void index(boolean bForce) throws SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		if (!bForce)
			if (!weMustIndexNow())
				return;
		lock.rl.lock();
		try {
			initWorking();
			indexWork();
			resetWork();
		} finally {
			lock.rl.unlock();
		}
	}
}
