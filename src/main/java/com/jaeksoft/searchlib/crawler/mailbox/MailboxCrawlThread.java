/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.mailbox.crawler.MailboxAbstractCrawler;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery.SearchField.Mode;

public class MailboxCrawlThread extends
		CrawlThreadAbstract<MailboxCrawlThread, MailboxCrawlMaster> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client client;

	private final List<IndexDocument> documents;

	protected long pendingIndexDocumentCount;

	protected long updatedIndexDocumentCount;

	protected long pendingDeleteDocumentCount;

	protected long updatedDeleteDocumentCount;

	protected long ignoredDocumentCount;

	protected long errorDocumentCount;

	private final WebCrawlMaster webCrawlMaster;

	private final ParserSelector parserSelector;

	private final MailboxCrawlItem mailboxCrawlItem;

	private final MailboxFieldMap mailboxFieldMap;

	private final CommonFieldTarget uniqueFieldTarget;

	private final SearchFieldRequest uniqueSearchRequest;

	private final LanguageEnum defaultLang;

	protected final InfoCallback infoCallback;

	protected final TaskLog taskLog;

	public MailboxCrawlThread(Client client, MailboxCrawlMaster crawlMaster,
			MailboxCrawlItem crawlItem, Variables variables,
			InfoCallback infoCallback) throws SearchLibException {
		super(client, crawlMaster, crawlItem);
		this.infoCallback = infoCallback;
		this.taskLog = infoCallback instanceof TaskLog ? (TaskLog) infoCallback
				: null;
		this.client = client;
		this.mailboxCrawlItem = crawlItem;
		defaultLang = crawlItem.getLang();
		webCrawlMaster = client.getWebCrawlMaster();
		parserSelector = client.getParserSelector();
		mailboxFieldMap = (MailboxFieldMap) crawlItem.getFieldMap();
		uniqueFieldTarget = mailboxFieldMap.getUniqueFieldTarget(client);
		if (uniqueFieldTarget != null) {
			uniqueSearchRequest = new SearchFieldRequest(client);
			uniqueSearchRequest.addSearchField(uniqueFieldTarget.getName(),
					Mode.TERM, 1.0F, 1.0F, 1);
			uniqueSearchRequest.setRows(0);
		} else
			uniqueSearchRequest = null;

		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		updatedDeleteDocumentCount = 0;
		ignoredDocumentCount = 0;
		errorDocumentCount = 0;
		this.documents = new ArrayList<IndexDocument>();
	}

	@Override
	public void runner() throws Exception {
		setStatusInfo(CrawlStatus.STARTING);
		MailboxAbstractCrawler crawler = MailboxProtocolEnum.getNewCrawler(
				this, mailboxCrawlItem);
		setStatusInfo(CrawlStatus.CRAWL);
		crawler.read();
		if (isAborted())
			return;
		index(documents, 0);
	}

	@Override
	protected String getCurrentInfo() {
		return getCountInfo();
	}

	public void indexContent(Object object, String contentType,
			IndexDocument indexDocument) throws SearchLibException,
			IOException, ClassNotFoundException {
		int i = contentType.indexOf(';');
		String contentBaseType = i == -1 ? contentType : contentType.substring(
				0, i);
		String fileName = null;
		InputStream inputStream;
		if (object instanceof String)
			inputStream = new ByteArrayInputStream(((String) object).getBytes());
		else if (object instanceof InputStream)
			inputStream = (InputStream) object;
		else {
			Logging.warn("Unknown content: " + object.getClass().getName()
					+ " ContentType: " + contentType);
			return;
		}
		Parser parser = parserSelector.parseStream(null, fileName,
				contentBaseType, null, inputStream, defaultLang, null, null);
		if (parser == null)
			return;
		List<ParserResultItem> results = parser.getParserResults();
		if (results == null)
			return;
		for (ParserResultItem result : results)
			result.populate(indexDocument);
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
		sb.append(") / ");
		sb.append(getIgnoredDocumentCount());
		sb.append(" / ");
		sb.append(getErrorDocumentCount());
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

	public final void incIgnored() {
		rwl.w.lock();
		try {
			ignoredDocumentCount++;
		} finally {
			rwl.w.unlock();
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

	public void addDocument(IndexDocument crawlDocument,
			IndexDocument parserIndexDocument) throws IOException,
			SearchLibException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			InstantiationException, IllegalAccessException {
		IndexDocument indexDocument = new IndexDocument(
				mailboxCrawlItem.getLang());
		((MailboxFieldMap) mailboxCrawlItem.getFieldMap()).mapIndexDocument(
				webCrawlMaster, parserSelector, null, crawlDocument,
				indexDocument);
		if (parserIndexDocument != null)
			indexDocument.add(parserIndexDocument);
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
		setStatusInfo(CrawlStatus.INDEXATION);
		client.updateDocuments(indexDocumentList);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount -= i;
			updatedIndexDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}
		indexDocumentList.clear();
		setStatusInfo(CrawlStatus.CRAWL);
		return true;
	}

	public void setStatusInfo(CrawlStatus status) {
		setStatus(status);
		StringBuilder sb = new StringBuilder(mailboxCrawlItem.getName());
		sb.append(' ');
		sb.append(getCountInfo());
		setInfo(sb.toString());
		if (infoCallback != null)
			infoCallback.setInfo(getStatusInfo());
	}

	@Override
	public boolean isAborted() {
		if (taskLog != null)
			if (taskLog.isAbortRequested())
				if (!super.isAborted())
					abort();
		return super.isAborted();
	}

	public boolean isAlreadyIndexed(String messageId) throws SearchLibException {
		if (uniqueFieldTarget == null)
			return false;
		String value = mailboxFieldMap.mapFieldTarget(uniqueFieldTarget,
				messageId);
		if (StringUtils.isEmpty(value))
			return false;
		uniqueSearchRequest.reset();
		uniqueSearchRequest.setQueryString(value);
		AbstractResultSearch result = (AbstractResultSearch) client
				.request(uniqueSearchRequest);
		return result.getNumFound() > 0;
	}

}
