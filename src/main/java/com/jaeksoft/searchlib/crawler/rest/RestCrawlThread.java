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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem.CallbackMode;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.CommonListResult;
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

	protected final InfoCallback infoCallback;

	private final Collection<String> idsCallback;

	public RestCrawlThread(Client client, RestCrawlMaster crawlMaster,
			RestCrawlItem restCrawlItem, Variables variables,
			InfoCallback infoCallback) {
		super(client, crawlMaster, restCrawlItem);
		this.restCrawlItem = restCrawlItem.duplicate();
		this.restCrawlItem.apply(variables);
		this.client = client;
		pendingIndexDocumentCount = 0;
		updatedIndexDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		pendingDeleteDocumentCount = 0;
		this.infoCallback = infoCallback;
		this.idsCallback = infoCallback != null
				&& infoCallback instanceof CommonListResult ? ((CommonListResult) infoCallback).items
				: null;
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

	public RestCrawlItem getRestCrawlItem() {
		return restCrawlItem;
	}

	@Override
	protected String getCurrentInfo() {
		return "";
	}

	private void callback(HttpDownloader downloader, URI uri, String query)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		uri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(),
				uri.getPath(), query, uri.getFragment());
		DownloadItem dlItem = downloader.request(uri,
				restCrawlItem.getCallbackMethod(),
				restCrawlItem.getCredential(), null, null, null);
		dlItem.checkNoError(200, 201, 202, 203);
	}

	private final void callbackPerDoc(HttpDownloader downloader, URI uri,
			String queryPrefix, String key) throws ClientProtocolException,
			IllegalStateException, IOException, URISyntaxException,
			SearchLibException {
		StringBuilder queryString = new StringBuilder();
		String query = uri.getQuery();
		if (query != null)
			queryString.append(query);
		if (!StringUtils.isEmpty(queryPrefix)) {
			if (queryString.length() != 0)
				queryString.append('&');
			queryString.append(queryPrefix);
			if (!StringUtils.isEmpty(key)) {
				queryString.append('=');
				queryString.append(key);
			}
		}
		callback(downloader, uri, queryString.toString());
	}

	private final void callbackAllDocs(HttpDownloader downloader, URI uri,
			String queryPrefix, List<String> pkList)
			throws ClientProtocolException, IllegalStateException, IOException,
			URISyntaxException, SearchLibException {
		StringBuilder queryString = new StringBuilder();
		String query = uri.getQuery();
		if (query != null)
			queryString.append(query);
		if (!StringUtils.isEmpty(queryPrefix) && pkList != null) {
			for (String key : pkList) {
				if (queryString.length() != 0)
					queryString.append('&');
				queryString.append(queryPrefix);
				queryString.append('=');
				queryString.append(key);
			}
		}
		callback(downloader, uri, queryString.toString());
	}

	private final void doCallBack(HttpDownloader downloader, List<String> pkList)
			throws ClientProtocolException, IllegalStateException, IOException,
			URISyntaxException, SearchLibException {
		CallbackMode mode = restCrawlItem.getCallbackMode();
		if (mode == CallbackMode.NO_CALL)
			return;
		String url = restCrawlItem.getCallbackUrl();
		String qp = restCrawlItem.getCallbackQueryParameter();
		URI uri = new URI(url);
		switch (mode) {
		case ONE_CALL_PER_DOCUMENT:
			if (pkList != null)
				for (String key : pkList)
					callbackPerDoc(downloader, uri, qp, key);
			break;
		case ONE_CALL_FOR_ALL_DOCUMENTS:
			callbackAllDocs(downloader, uri, qp, pkList);
			break;
		default:
			break;
		}

	}

	private final boolean index(HttpDownloader downloader,
			List<IndexDocument> indexDocumentList, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int i = indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.INDEXATION);
		client.updateDocuments(indexDocumentList);
		SchemaField uniqueField = client.getSchema().getFieldList()
				.getUniqueField();
		List<String> pkList = null;
		if (uniqueField != null) {
			pkList = new ArrayList<String>(indexDocumentList.size());
			String fieldName = uniqueField.getName();
			for (IndexDocument indexDocument : indexDocumentList)
				pkList.add(indexDocument.getFieldValueString(fieldName, 0));
			if (idsCallback != null)
				idsCallback.addAll(pkList);
		}
		doCallBack(downloader, pkList);
		rwl.w.lock();
		try {
			pendingIndexDocumentCount -= i;
			updatedIndexDocumentCount += i;
		} finally {
			rwl.w.unlock();
		}
		indexDocumentList.clear();
		if (infoCallback != null) {
			infoCallback.setInfo(updatedIndexDocumentCount
					+ " document(s) indexed");
		}
		return true;
	}

	@Override
	public void runner() throws Exception {
		HttpDownloader downloader = null;
		setStatus(CrawlStatus.STARTING);
		try {
			URI uri = new URI(restCrawlItem.getUrl());
			downloader = getConfig().getWebCrawlMaster().getNewHttpDownloader(
					true);
			setStatus(CrawlStatus.CRAWL);
			DownloadItem dlItem = downloader.request(uri,
					restCrawlItem.getMethod(), restCrawlItem.getCredential(),
					null, null, null);
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
				index(downloader, indexDocumentList, limit);
			}
			index(downloader, indexDocumentList, 0);

		} finally {
			if (downloader != null)
				downloader.release();
		}
	}
}
