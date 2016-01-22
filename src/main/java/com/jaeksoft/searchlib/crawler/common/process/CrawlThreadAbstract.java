/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.util.InfoCallback;

public abstract class CrawlThreadAbstract<T extends CrawlThreadAbstract<T, M>, M extends CrawlMasterAbstract<M, T>>
		extends ThreadAbstract<T> {

	private volatile CrawlStatus status;

	protected volatile CrawlStatistics currentStats;

	protected CrawlThreadAbstract(Config config, M crawlMaster, ThreadItem<?, T> uniqueThreadItem,
			InfoCallback infoCallback) {
		super(config, crawlMaster, uniqueThreadItem, infoCallback);
		currentStats = null;
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	public CrawlStatus getStatus() {
		synchronized (this) {
			return status;
		}
	}

	public String getStatusInfo() {
		synchronized (this) {
			StringBuilder sb = new StringBuilder();
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
			idle();
			this.status = status;
		}
	}

	public String getDebugInfo() {
		synchronized (this) {
			StringBuilder sb = new StringBuilder();
			sb.append(getThreadStatus());
			sb.append(' ');
			sb.append(getCurrentInfo());
			return sb.toString();
		}
	}

	protected abstract String getCurrentInfo();

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
	}

	@Override
	public void release() {
		Exception e = getException();
		if (e != null) {
			Logging.error(e.getMessage(), e);
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage() == null ? e.toString() : e.getMessage());
		} else {
			if (isAborted())
				setStatus(CrawlStatus.ABORTED);
			else
				setStatus(CrawlStatus.COMPLETE);
		}
	}

	@Override
	protected void sleepMs(long ms) throws InterruptedException {
		setStatus(CrawlStatus.WAITING);
		super.sleepMs(ms);
	}

}
