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

package com.jaeksoft.searchlib.crawler.mailbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.mailbox.crawler.MailboxAbstractCrawler;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;

public class MailboxCrawlThread extends
		CrawlThreadAbstract<MailboxCrawlThread, MailboxCrawlMaster> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client client;

	private final List<IndexDocument> documents;

	protected long pendingIndexDocumentCount;

	protected long updatedIndexDocumentCount;

	protected long pendingDeleteDocumentCount;

	protected long updatedDeleteDocumentCount;

	protected long errorDocumentCount;

	private final MailboxCrawlItem mailboxCrawlItem;

	public MailboxCrawlThread(Client client, MailboxCrawlMaster crawlMaster,
			MailboxCrawlItem crawlItem, Variables variables,
			InfoCallback infoCallback) {
		super(client, crawlMaster, crawlItem);
		this.client = client;
		this.mailboxCrawlItem = crawlItem;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		updatedDeleteDocumentCount = 0;
		errorDocumentCount = 0;
		this.documents = new ArrayList<IndexDocument>();
	}

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		MailboxAbstractCrawler crawler = MailboxProtocolEnum.getNewCrawler(
				this, mailboxCrawlItem);
		crawler.read();
		if (isAborted())
			return;
		index(documents, 0);
	}

	@Override
	protected String getCurrentInfo() {
		return "";
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

	public void addDocument(IndexDocument crawlDocument, Object content)
			throws IOException, SearchLibException {
		IndexDocument indexDocument = new IndexDocument(
				mailboxCrawlItem.getLang());
		((MailboxFieldMap) mailboxCrawlItem.getFieldMap()).mapIndexDocument(
				crawlDocument, indexDocument);
		documents.add(indexDocument);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount++;
		} finally {
			rwl.w.unlock();
		}
		index(documents, mailboxCrawlItem.getBufferSize());
	}

	final public void incError() {
		rwl.w.lock();
		try {
			errorDocumentCount++;
		} finally {
			rwl.w.unlock();
		}
	}

	final public long getErrorDocumentCount() {
		rwl.r.lock();
		try {
			return errorDocumentCount;
		} finally {
			rwl.r.unlock();
		}
	}

	private final boolean index(List<IndexDocument> indexDocumentList, int limit)
			throws IOException, SearchLibException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.INDEXATION);
		client.updateDocuments(indexDocumentList);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount -= i;
			updatedIndexDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}
		indexDocumentList.clear();
		setInfo(updatedIndexDocumentCount + " document(s) indexed");
		return true;
	}

}
