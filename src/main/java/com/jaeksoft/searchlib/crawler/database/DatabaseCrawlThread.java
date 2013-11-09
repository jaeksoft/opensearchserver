/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public abstract class DatabaseCrawlThread extends
		CrawlThreadAbstract<DatabaseCrawlThread, DatabaseCrawlMaster> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected final Client client;

	private final DatabaseCrawlAbstract databaseCrawl;

	protected long pendingIndexDocumentCount;

	protected long updatedIndexDocumentCount;

	protected long pendingDeleteDocumentCount;

	protected long updatedDeleteDocumentCount;

	protected final InfoCallback infoCallback;

	public DatabaseCrawlThread(Client client, DatabaseCrawlMaster crawlMaster,
			DatabaseCrawlAbstract databaseCrawl, InfoCallback infoCallback) {
		super(client, crawlMaster, databaseCrawl);
		this.databaseCrawl = databaseCrawl;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		this.infoCallback = infoCallback;
	}

	public String getCountInfo() {
		StringBuilder sb = new StringBuilder();
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

	public DatabaseCrawlAbstract getDatabaseCrawl() {
		return databaseCrawl;
	}

	@Override
	protected String getCurrentInfo() {
		return "";
	}

}
