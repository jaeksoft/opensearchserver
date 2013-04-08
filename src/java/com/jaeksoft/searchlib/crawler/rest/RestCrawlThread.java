/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.rest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class RestCrawlThread extends
		CrawlThreadAbstract<RestCrawlThread, RestCrawlMaster> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected final Client client;

	private final RestCrawlItem restCrawlItem;

	protected long pendingIndexDocumentCount;

	protected long updatedIndexDocumentCount;

	protected long pendingDeleteDocumentCount;

	protected long updatedDeleteDocumentCount;

	protected final TaskLog taskLog;

	public RestCrawlThread(Client client, RestCrawlMaster crawlMaster,
			RestCrawlItem restCrawlItem, TaskLog taskLog) {
		super(client, crawlMaster, restCrawlItem);
		this.restCrawlItem = restCrawlItem;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		this.taskLog = taskLog;
	}

	public String getCountInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append(getUpdatedIndexDocumentCount());
		sb.append(" (");
		sb.append(getPendingIndexDocumentCount());
		sb.append(") / ");
		sb.append(getUpdatedDeleteDocumentCount());
		sb.append(" (");
		sb.append(getPendingDeleteDocumentCount());
		sb.append(')');
		return sb.toString();
	}

	final public long getPendingIndexDocumentCount() {
		rwl.r.lock();
		try {
			return pendingIndexDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getUpdatedIndexDocumentCount() {
		rwl.r.lock();
		try {
			return updatedIndexDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getPendingDeleteDocumentCount() {
		rwl.r.lock();
		try {
			return pendingDeleteDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getUpdatedDeleteDocumentCount() {
		rwl.r.lock();
		try {
			return updatedDeleteDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	public RestCrawlItem getRestCrawlItem() {
		return restCrawlItem;
	}

	@Override
	protected String getCurrentInfo() {
		return "";
	}

	@Override
	public void runner() throws Exception {
		// TODO Auto-generated method stub

	}

}
