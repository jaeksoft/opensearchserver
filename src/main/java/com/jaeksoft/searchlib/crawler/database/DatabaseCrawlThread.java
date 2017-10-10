/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.crawler.database;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;

import java.io.IOException;
import java.util.List;

public abstract class DatabaseCrawlThread extends CrawlThreadAbstract<DatabaseCrawlThread, DatabaseCrawlMaster> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected final Client client;

	private final DatabaseCrawlAbstract databaseCrawl;

	protected long pendingIndexDocumentCount;

	protected long updatedIndexDocumentCount;

	protected long pendingDeleteDocumentCount;

	protected long updatedDeleteDocumentCount;

	protected long ignoredDocumentCount;

	public DatabaseCrawlThread(Client client, DatabaseCrawlMaster crawlMaster, DatabaseCrawlAbstract databaseCrawl,
			InfoCallback infoCallback) {
		super(client, "Database Crawl: " + databaseCrawl.getName(), crawlMaster, databaseCrawl, infoCallback);
		this.databaseCrawl = databaseCrawl;
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		ignoredDocumentCount = 0;
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
		sb.append(" / ");
		sb.append(getIgnoredDocumentCount());
		return sb.toString();
	}

	protected boolean index(List<IndexDocument> indexDocumentList, int limit)
			throws IOException, SearchLibException, InterruptedException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		client.updateDocuments(indexDocumentList);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount -= i;
			updatedIndexDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}
		indexDocumentList.clear();
		if (infoCallback != null)
			infoCallback.setInfo(updatedIndexDocumentCount + " document(s) indexed");
		sleepMs(databaseCrawl.getMsSleep());
		return true;
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

	final public long getIgnoredDocumentCount() {
		rwl.r.lock();
		try {
			return ignoredDocumentCount;
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
