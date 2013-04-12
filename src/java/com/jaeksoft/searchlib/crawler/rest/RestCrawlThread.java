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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jayway.jsonpath.JsonPath;

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

	private final boolean index(List<IndexDocument> indexDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
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
		if (taskLog != null)
			taskLog.setInfo(updatedIndexDocumentCount + " document(s) indexed");
		return true;
	}

	@Override
	public void runner() throws Exception {
		setStatus(CrawlStatus.STARTING);
		HttpDownloader downloader = null;
		try {
			URI uri = new URI(restCrawlItem.getUrl());
			CredentialItem credentialItem = restCrawlItem.getCredential();
			downloader = getConfig().getWebCrawlMaster().getNewHttpDownloader();
			setStatus(CrawlStatus.CRAWL);
			DownloadItem dlItem = downloader.get(uri, credentialItem);
			JsonPath path = JsonPath.compile(restCrawlItem.getPathDocument());
			RestFieldMap restFieldMap = restCrawlItem.getFieldMap();
			LanguageEnum lang = restCrawlItem.getLang();
			List<IndexDocument> indexDocumentList = new ArrayList<IndexDocument>(
					0);
			int limit = restCrawlItem.getBufferSize();
			List<Object> documents = path.read(dlItem.getContentInputStream());
			if (documents == null)
				return;
			for (Object document : documents) {
				setStatus(CrawlStatus.CRAWL);
				IndexDocument newIndexDocument = new IndexDocument(lang);
				restFieldMap.mapJson(client.getWebCrawlMaster(),
						client.getParserSelector(), lang, document,
						newIndexDocument);
				indexDocumentList.add(newIndexDocument);
				rwl.w.lock();
				try {
					pendingIndexDocumentCount++;
				} finally {
					rwl.w.unlock();
				}
				index(indexDocumentList, limit);
			}
			index(indexDocumentList, 0);

		} finally {
			if (downloader != null)
				downloader.release();
		}
	}
}
