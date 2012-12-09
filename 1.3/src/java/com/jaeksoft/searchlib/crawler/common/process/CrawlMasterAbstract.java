/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.LinkedList;
import java.util.List;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.plugin.IndexPluginList;
import com.jaeksoft.searchlib.process.ThreadMasterAbstract;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public abstract class CrawlMasterAbstract extends ThreadMasterAbstract {

	private final LinkedList<CrawlStatistics> statistics;

	private IndexPluginList indexPluginList;

	private CrawlStatus status;

	protected CrawlStatistics currentStats;

	private final ReadWriteLock rwl = new ReadWriteLock();

	private boolean runOnce;

	protected CrawlMasterAbstract(Config config) {
		super(config);
		status = CrawlStatus.NOT_RUNNING;
		statistics = new LinkedList<CrawlStatistics>();
	}

	public void start(boolean once) {
		if (isRunning())
			return;
		try {
			setOnce(once);
			setStatus(CrawlStatus.STARTING);
			createIndexPluginList();
		} catch (SearchLibException e) {
			Logging.error(e.getMessage(), e);
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage());
			return;
		}
		execute();
	}

	private void createIndexPluginList() throws SearchLibException {
		rwl.w.lock();
		try {
			indexPluginList = new IndexPluginList(getConfig()
					.getIndexPluginTemplateList());

		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isOnce() {
		rwl.r.lock();
		try {
			return runOnce;
		} finally {
			rwl.r.unlock();
		}
	}

	public CrawlStatus getStatus() {
		rwl.r.lock();
		try {
			return status;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getStatusInfo() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(status);
			String info = getInfo();
			if (info != null) {
				sb.append(' ');
				sb.append('(');
				sb.append(info);
				sb.append(')');
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setStatus(CrawlStatus status) {
		rwl.w.lock();
		try {
			this.status = status;
		} finally {
			rwl.w.unlock();
		}
	}

	private void setOnce(boolean runOnce) {
		rwl.w.lock();
		try {
			this.runOnce = runOnce;
		} finally {
			rwl.w.unlock();
		}
	}

	protected void addStatistics(CrawlStatistics stats) {
		rwl.w.lock();
		try {
			if (statistics.size() >= 10)
				statistics.removeLast();
			statistics.addFirst(stats);
		} finally {
			rwl.w.unlock();
		}
	}

	public List<CrawlStatistics> getStatistics() {
		rwl.r.lock();
		try {
			return statistics;
		} finally {
			rwl.r.unlock();
		}
	}

	public IndexPluginList getIndexPluginList() {
		rwl.r.lock();
		try {
			return indexPluginList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void release() {
		Exception e = getException();
		if (e != null) {
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage() == null ? e.toString() : e.getMessage());
		} else {
			if (isAborted())
				setStatus(CrawlStatus.ABORTED);
			else
				setStatus(CrawlStatus.COMPLETE);
		}
	}

	public abstract CrawlQueueAbstract getCrawlQueue();

}
