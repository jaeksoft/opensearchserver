/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import java.util.LinkedList;
import java.util.List;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.plugin.IndexPluginList;
import com.jaeksoft.searchlib.process.ThreadMasterAbstract;

public abstract class CrawlMasterAbstract extends ThreadMasterAbstract {

	private final LinkedList<CrawlStatistics> statistics;

	private IndexPluginList indexPluginList;

	private CrawlStatus status;

	protected CrawlStatistics currentStats;

	protected CrawlMasterAbstract(Config config) {
		super(config);
		status = CrawlStatus.NOT_RUNNING;
		statistics = new LinkedList<CrawlStatistics>();
	}

	public void start() {
		if (isRunning())
			return;
		try {
			setStatus(CrawlStatus.STARTING);
			indexPluginList = new IndexPluginList(getConfig()
					.getIndexPluginTemplateList());
		} catch (SearchLibException e) {
			Logging.logger.error(e.getMessage(), e);
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage());
			return;
		}
		execute();
	}

	public CrawlStatus getStatus() {
		synchronized (this) {
			return status;
		}
	}

	public String getStatusInfo() {
		synchronized (this) {
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
		}
	}

	public void setStatus(CrawlStatus status) {
		synchronized (this) {
			this.status = status;
		}
	}

	protected void addStatistics(CrawlStatistics stats) {
		synchronized (statistics) {
			if (statistics.size() >= 10)
				statistics.removeLast();
			statistics.addFirst(stats);
		}
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	public IndexPluginList getIndexPluginList() {
		return indexPluginList;
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
