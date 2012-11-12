/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadAbstract;

public abstract class CrawlThreadAbstract extends ThreadAbstract {

	private volatile CrawlStatus status;

	protected volatile CrawlStatistics currentStats;

	protected CrawlThreadAbstract(Config config, CrawlMasterAbstract crawlMaster) {
		super(config, crawlMaster);
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
			idle();
			this.status = status;
		}
	}

	public String getDebugInfo() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			sb.append(getThreadStatus());
			sb.append(' ');
			sb.append(getCurrentInfo());
			return sb.toString();
		}
	}

	final protected CrawlMasterAbstract getCrawlMasterAbstract() {
		return (CrawlMasterAbstract) getThreadMaster();
	}

	protected abstract String getCurrentInfo();

	public CrawlStatistics getCurrentStatistics() {
		return currentStats;
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

	@Override
	protected void sleepMs(long ms) {
		setStatus(CrawlStatus.WAITING);
		super.sleepMs(ms);
	}

}
