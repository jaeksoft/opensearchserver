/**   

 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import tokyocabinet.TDB;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.Field;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;

public class UrlManagerTokyo extends UrlManagerAbstract {

	public TDB tdb;

	private Client targetClient;

	public UrlManagerTokyo() {
		tdb = new TDB();
	}

	@Override
	public void init(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		targetClient = client;
	}

	@Override
	public Client getUrlDbClient() {
		return null;
	}

	public void deleteUrl(String sUrl) throws SearchLibException {
		try {
			if (sUrl == null)
				return;
			targetClient.deleteDocument(sUrl);
			// TODO Implementation
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (HttpException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void deleteUrls(Collection<String> workDeleteUrlList)
			throws SearchLibException {
		try {
			targetClient.deleteDocuments(workDeleteUrlList);
			// TODO Implementation

		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public boolean exists(String sUrl) throws SearchLibException {
		// TODO Implementation
		return false;
	}

	@Override
	public void getOldHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		// TODO Implementation

	}

	@Override
	public void getNewHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		// TODO Implementation

	}

	@Override
	public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		// TODO Implementation

	}

	@Override
	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		// TODO Implementation
		return null;
	}

	@Override
	public void getNewUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		// TODO Implementation

	}

	@Override
	public SearchRequest urlQuery(SearchTemplate urlSearchTemplate,
			String like, String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate)
			throws SearchLibException {
		// TODO Implementation
		return null;
	}

	@Override
	public void reload(boolean optimize) throws SearchLibException {
		// TODO Implementation
		targetClient.reload();
	}

	@Override
	public void updateUrlItems(List<UrlItem> urlItems)
			throws SearchLibException {
		try {
			if (urlItems == null)
				return;
			List<IndexDocument> documents = new ArrayList<IndexDocument>(
					urlItems.size());
			for (UrlItem urlItem : urlItems) {
				if (urlItem == null)
					continue;

				IndexDocument indexDocument = new IndexDocument();
				urlItem.populate(indexDocument);
				documents.add(indexDocument);
			}

			for (UrlItem urlItem : urlItems) {
				if (urlItem == null)
					continue;
				dbOpen();

				Map<String, String> cols = new HashMap<String, String>();
				cols.put("url", urlItem.getUrl());
				cols.put("lang", urlItem.getLang());
				cols.put("langMethod", urlItem.getLangMethod());
				cols.put("contentBaseType", urlItem.getContentBaseType());
				cols.put("contentTypeCharset", urlItem.getContentTypeCharset());
				cols.put("contentEncoding", urlItem.getContentEncoding());
				cols.put("url", urlItem.getUrl());
				cols.put("host", urlItem.getHost());
				cols.put("when", Long.toString(urlItem.getWhen().getTime()));
				cols.put("responseCode", urlItem.getResponseCode().toString());
				cols.put("robotsTxtStatus", urlItem.getRobotsTxtStatus().name());
				cols.put("parserStatus", urlItem.getParserStatus().name());
				cols.put("fetchStatus", urlItem.getFetchStatus().name());
				cols.put("indexStatus", urlItem.getIndexStatus().name());

				if (!tdb.put(urlItem.getUrl(), cols)) {
					int ecode = tdb.ecode();
					System.err.println("put error: " + TDB.errmsg(ecode));
				}

			}

		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			dbClose();
		}
	}

	public void dbOpen() {
		if (!tdb.open("urls.oss", TDB.OWRITER | TDB.OCREAT)) {
			int ecode = tdb.ecode();
			System.err.println("open error: " + TDB.errmsg(ecode));
		}
	}

	public void dbClose() {
		if (!tdb.close()) {
			int ecode = tdb.ecode();
			System.err.println("close error: " + TDB.errmsg(ecode));
		}
	}

	@Override
	public void inject(List<InjectUrlItem> list) throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getUrls(SearchRequest searchRequest, Field orderBy,
			boolean orderAsc, long start, long rows, List<UrlItem> list)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return 0;
	}

}