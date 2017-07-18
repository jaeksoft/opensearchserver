/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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
 **/

package com.jaeksoft.searchlib.crawler.rest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.rest.RestCrawlItem.CallbackMode;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader.Method;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RestCrawlThread extends CrawlThreadAbstract<RestCrawlThread, RestCrawlMaster> {

	protected final Client client;

	private final RestCrawlItem restCrawlItem;

	private final AtomicLong pendingIndexDocumentCount;

	private final AtomicLong updatedIndexDocumentCount;

	private final Collection<String> idsCallback;

	private final FieldMapContext fieldMapContext;

	private static class RestCrawlContext {

		private final HttpDownloader downloader;
		private final List<IndexDocument> indexDocumentList;
		private final RestFieldMap restFieldMap;
		private final int bufferSize;
		private final JsonPath jsonPath;

		private RestCrawlContext(HttpDownloader downloader, RestCrawlItem restCrawlItem) throws SearchLibException {
			this.downloader = downloader;
			jsonPath = JsonPath.compile(restCrawlItem.getPathDocument());
			restFieldMap = restCrawlItem.getFieldMap();
			bufferSize = restCrawlItem.getBufferSize();
			indexDocumentList = new ArrayList<IndexDocument>(bufferSize);
		}
	}

	@SuppressWarnings("unchecked")
	public RestCrawlThread(Client client, RestCrawlMaster crawlMaster, RestCrawlItem restCrawlItem, Variables variables,
			InfoCallback infoCallback) throws SearchLibException {
		super(client, crawlMaster, restCrawlItem, infoCallback);
		this.restCrawlItem = restCrawlItem.duplicate();
		this.restCrawlItem.apply(variables);
		this.client = client;
		pendingIndexDocumentCount = new AtomicLong();
		updatedIndexDocumentCount = new AtomicLong();
		fieldMapContext = new FieldMapContext(client, restCrawlItem.getLang());
		this.idsCallback = infoCallback != null && infoCallback instanceof CommonListResult<?> ?
				((CommonListResult<String>) infoCallback).items :
				null;
	}

	public String getCountInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(getUpdatedIndexDocumentCount());
		sb.append(" (");
		sb.append(getPendingIndexDocumentCount());
		sb.append(")");
		return sb.toString();
	}

	final public long getPendingIndexDocumentCount() {
		return pendingIndexDocumentCount.get();
	}

	final public long getUpdatedIndexDocumentCount() {
		return updatedIndexDocumentCount.get();
	}

	public RestCrawlItem getRestCrawlItem() {
		return restCrawlItem;
	}

	@Override
	protected String getCurrentInfo() {
		return "";
	}

	private void callback(HttpDownloader downloader, URI uri, String query)
			throws URISyntaxException, ClientProtocolException, IllegalStateException, IOException, SearchLibException {
		uri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
		DownloadItem dlItem = downloader.request(uri, restCrawlItem.getCallbackMethod(), restCrawlItem.getCredential(),
				null, null, null);
		dlItem.checkNoErrorList(200, 201, 202, 203);
	}

	private final void callbackPerDoc(HttpDownloader downloader, URI uri, String queryPrefix, String key)
			throws ClientProtocolException, IllegalStateException, IOException, URISyntaxException, SearchLibException {
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

	private final void callbackAllDocs(HttpDownloader downloader, URI uri, String queryPrefix, List<String> pkList)
			throws ClientProtocolException, IllegalStateException, IOException, URISyntaxException, SearchLibException {
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
			throws ClientProtocolException, IllegalStateException, IOException, URISyntaxException, SearchLibException {
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

	private final boolean index(RestCrawlContext context, int limit)
			throws NoSuchAlgorithmException, IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		int i = context.indexDocumentList.size();
		if (i == 0 || i < limit)
			return false;
		setStatus(CrawlStatus.INDEXATION);
		client.updateDocuments(context.indexDocumentList);
		SchemaField uniqueField = client.getSchema().getFieldList().getUniqueField();
		List<String> pkList = null;
		if (uniqueField != null) {
			pkList = new ArrayList<String>(context.indexDocumentList.size());
			String fieldName = uniqueField.getName();
			for (IndexDocument indexDocument : context.indexDocumentList)
				pkList.add(indexDocument.getFieldValueString(fieldName, 0));
			if (idsCallback != null)
				idsCallback.addAll(pkList);
		}
		doCallBack(context.downloader, pkList);
		pendingIndexDocumentCount.addAndGet(-i);
		updatedIndexDocumentCount.addAndGet(i);
		context.indexDocumentList.clear();
		if (infoCallback != null)
			infoCallback.setInfo(updatedIndexDocumentCount + " document(s) indexed");
		return true;
	}

	private void runDocument(RestCrawlContext context, Object document) throws Exception {
		setStatus(CrawlStatus.CRAWL);
		IndexDocument newIndexDocument = new IndexDocument(fieldMapContext.lang);
		context.restFieldMap.mapJson(fieldMapContext, document, newIndexDocument);
		context.indexDocumentList.add(newIndexDocument);
		pendingIndexDocumentCount.incrementAndGet();
		if (index(context, context.bufferSize))
			setStatus(CrawlStatus.CRAWL);
	}

	private int runDocumentList(RestCrawlContext context, Object jsonDoc) throws Exception {
		if (jsonDoc == null)
			return 0;
		if (jsonDoc instanceof Map<?, ?>) {
			runDocument(context, jsonDoc);
			return 1;
		}
		if (jsonDoc instanceof List<?>) {
			List<?> documents = (List<?>) jsonDoc;
			for (Object document : documents)
				runDocument(context, document);
			return documents.size();
		}
		return 0;
	}

	private int runDownload(RestCrawlContext context, URI uri) throws Exception {
		DownloadItem dlItem = context.downloader.request(uri, restCrawlItem.getMethod(), restCrawlItem.getCredential(),
				null, null, null);
		try {
			List<Object> documents = context.jsonPath.read(dlItem.getContentInputStream());
			return runDocumentList(context, documents);
		} catch (PathNotFoundException e) {
			return 0;
		}
	}

	private int runFile(RestCrawlContext context, File file) throws Exception {
		int res;
		try {
			res = runDocumentList(context, context.jsonPath.read(file));
		} catch (PathNotFoundException e) {
			res = 0;
		}
		if (restCrawlItem.getMethod() == Method.DELETE)
			FileUtils.delete(file);
		return res;
	}

	private int runFiles(RestCrawlContext context, URI uri) throws Exception {
		File rootFile = new File(uri);
		if (rootFile.isFile())
			return runFile(context, rootFile);
		int res = 0;
		for (File file : rootFile.listFiles((FileFilter) FileFileFilter.FILE))
			res += runFile(context, file);
		return res;
	}

	private int runURL(RestCrawlContext context, URI uri) throws Exception {
		setStatus(CrawlStatus.CRAWL);
		if ("file".equals(uri.getScheme()))
			return runFiles(context, uri);
		else
			return runDownload(context, uri);
	}

	private void runSequence(RestCrawlContext context) throws Exception {
		Integer start = restCrawlItem.getSequenceFromInclusive();
		if (start == null)
			start = 0;
		Integer end = restCrawlItem.getSequenceToExclusive();
		if (end == null)
			end = 100;
		Integer inc = restCrawlItem.getSequenceIncrement();
		if (inc == null)
			inc = 1;
		for (int i = start; i < end; i += inc) {
			URIBuilder uriBuilder = new URIBuilder(restCrawlItem.getUrl());
			uriBuilder.addParameter(restCrawlItem.getSequenceParameter(), Integer.toString(i));
			if (runURL(context, uriBuilder.build()) == 0)
				break;
		}
	}

	@Override
	public void runner() throws Exception {
		HttpDownloader downloader = getConfig().getWebCrawlMaster().getNewHttpDownloader(true);
		setStatus(CrawlStatus.STARTING);
		try {
			RestCrawlContext context = new RestCrawlContext(downloader, restCrawlItem);
			if (StringUtils.isEmpty(restCrawlItem.getSequenceParameter()))
				runURL(context, new URI(restCrawlItem.getUrl()));
			else
				runSequence(context);
			index(context, 0);
		} finally {
			if (downloader != null)
				downloader.release();
		}
	}
}
