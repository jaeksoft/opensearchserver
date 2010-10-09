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
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.InjectUrlItem.Status;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;

public class UrlManager extends UrlManagerAbstract {

	public enum SearchTemplate {
		urlSearch, urlExport;
	}

	public enum Field {

		URL("url"), WHEN("when"), CONTENTBASETYPE("contentBaseType"), CONTENTTYPECHARSET(
				"contentTypeCharset"), CONTENTENCODING("contentEncoding"), CONTENTLENGTH(
				"contentLength"), LANG("lang"), LANGMETHOD("langMethod"), ROBOTSTXTSTATUS(
				"robotsTxtStatus"), FETCHSTATUS("fetchStatus"), RESPONSECODE(
				"responseCode"), PARSERSTATUS("parserStatus"), INDEXSTATUS(
				"indexStatus"), HOST("host"), SUBHOST("subhost");

		private String name;

		private Field(String name) {
			this.name = name;
		}

		private void addFilterQuery(SearchRequest request, Object value)
				throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQuery(sb, value);
			request.addFilter(sb.toString());
		}

		private void addFilterRange(SearchRequest request, Object from,
				Object to) throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQueryRange(sb, from, to);
			request.addFilter(sb.toString());
		}

		private void addQuery(StringBuffer sb, Object value) {
			sb.append(" ");
			sb.append(name);
			sb.append(":");
			sb.append(value);
		}

		private void addQueryRange(StringBuffer sb, Object from, Object to) {
			sb.append(" ");
			sb.append(name);
			sb.append(":[");
			sb.append(from);
			sb.append(" TO ");
			sb.append(to);
			sb.append("]");
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private Client urlDbClient;

	private Client targetClient;

	public UrlManager(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		this.urlDbClient = new Client(dataDir, "/url_config.xml", true);
		targetClient = client;
	}

	@Override
	public Client getUrlDbClient() {
		return urlDbClient;
	}

	@Override
	public void injectPrefix(List<PatternItem> patternList)
			throws SearchLibException {
		Iterator<PatternItem> it = patternList.iterator();
		List<InjectUrlItem> urlList = new ArrayList<InjectUrlItem>();
		while (it.hasNext()) {
			PatternItem item = it.next();
			if (item.getStatus() == PatternItem.Status.INJECTED)
				urlList.add(new InjectUrlItem(item));
		}
		inject(urlList);
	}

	public void deleteUrl(String sUrl) throws SearchLibException {
		try {
			if (sUrl == null)
				return;
			targetClient.deleteDocument(sUrl);
			urlDbClient.deleteDocument(sUrl);
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
			urlDbClient.deleteDocuments(workDeleteUrlList);
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
		SearchRequest request = getUrlSearchRequest();
		request.setQueryString("url:\"" + sUrl + '"');
		return (getUrls(request, null, false, 0, 0, null) > 0);
	}

	public void inject(List<InjectUrlItem> list) throws SearchLibException {
		synchronized (this) {
			try {
				List<IndexDocument> injectList = new ArrayList<IndexDocument>();
				for (InjectUrlItem item : list) {
					if (exists(item.getUrl()))
						item.setStatus(InjectUrlItem.Status.ALREADY);
					else
						injectList.add(item.getIndexDocument());
				}
				if (injectList.size() == 0)
					return;
				urlDbClient.updateDocuments(injectList);
				int injected = 0;
				for (InjectUrlItem item : list) {
					if (item.getStatus() == Status.UNDEFINED) {
						item.setStatus(Status.INJECTED);
						injected++;
					}
				}
				if (injected > 0)
					urlDbClient.reload();
			} catch (NoSuchAlgorithmException e) {
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
	}

	private void filterQueryToFetchOld(SearchRequest request,
			Date fetchIntervalDate) throws ParseException {
		StringBuffer query = new StringBuffer();
		query.append("when:[00000000000000 TO ");
		query.append(UrlItem.getWhenDateFormat().format(fetchIntervalDate));
		query.append("]");
		request.addFilter(query.toString());
	}

	private void filterQueryToFetchNew(SearchRequest request,
			Date fetchIntervalDate) throws ParseException {
		StringBuffer query = new StringBuffer();
		query.append("fetchStatus:");
		query.append(FetchStatus.UN_FETCHED.value);
		request.addFilter(query.toString());
		query = new StringBuffer();
		query.append("when:[");
		query.append(UrlItem.getWhenDateFormat().format(fetchIntervalDate));
		query.append(" TO 99999999999999]");
		request.addFilter(query.toString());
	}

	private void getFacetLimit(Field field, SearchRequest searchRequest,
			int limit, List<NamedItem> list) throws SearchLibException {
		Result result = urlDbClient.search(searchRequest);
		Facet facet = result.getFacetList().getByField(field.name);
		for (FacetItem facetItem : facet) {
			if (limit-- == 0)
				break;
			if (facetItem.getCount() == 0)
				continue;
			String term = facetItem.getTerm();
			if (term == null)
				continue;
			if (term.length() == 0)
				continue;
			synchronized (list) {
				list.add(new NamedItem(term, facetItem.getCount()));
			}
		}

	}

	private SearchRequest getHostFacetSearchRequest() {
		SearchRequest searchRequest = urlDbClient.getNewSearchRequest();
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.getFacetFieldList().add(
				new FacetField("host", 1, false, false));
		return searchRequest;
	}

	private SearchRequest getUrlSearchRequest() throws SearchLibException {
		SearchRequest searchRequest = urlDbClient.getNewSearchRequest();
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.addReturnField("url");
		searchRequest.addReturnField("host");
		searchRequest.addReturnField("contentBaseType");
		searchRequest.addReturnField("contentTypeCharset");
		searchRequest.addReturnField("contentEncoding");
		searchRequest.addReturnField("contentLength");
		searchRequest.addReturnField("lang");
		searchRequest.addReturnField("langMethod");
		searchRequest.addReturnField("when");
		searchRequest.addReturnField("responseCode");
		searchRequest.addReturnField("robotsTxtStatus");
		searchRequest.addReturnField("parserStatus");
		searchRequest.addReturnField("fetchStatus");
		searchRequest.addReturnField("indexStatus");
		return searchRequest;
	}

	@Override
	public void getOldHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		SearchRequest searchRequest = getHostFacetSearchRequest();
		searchRequest.setQueryString("*:*");
		try {
			filterQueryToFetchOld(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		getFacetLimit(Field.HOST, searchRequest, limit, hostList);
	}

	@Override
	public void getNewHostToFetch(Date fetchIntervalDate, int limit,
			List<NamedItem> hostList) throws SearchLibException {
		SearchRequest searchRequest = getHostFacetSearchRequest();
		searchRequest.setQueryString("*:*");
		try {
			filterQueryToFetchNew(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		getFacetLimit(Field.HOST, searchRequest, limit, hostList);
	}

	public void getStartingWith(String queryString, Field field, String start,
			int limit, List<NamedItem> list) throws ParseException,
			IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {
		SearchRequest searchRequest = urlDbClient.getNewSearchRequest(field
				+ "Facet");
		searchRequest.setQueryString(queryString);
		searchRequest.getFilterList().add(field + ":" + start + "*",
				Source.REQUEST);
		getFacetLimit(field, searchRequest, limit, list);
	}

	@Override
	public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		SearchRequest searchRequest = urlDbClient
				.getNewSearchRequest("urlSearch");
		try {
			searchRequest.addFilter("host:\""
					+ SearchRequest.escapeQuery(host.getName()) + "\"");
			searchRequest.setQueryString("*:*");
			filterQueryToFetchOld(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setRows((int) limit);
		Result result = urlDbClient.search(searchRequest);
		for (ResultDocument item : result)
			urlList.add(new UrlItem(item));
	}

	@Override
	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		SearchRequest searchRequest = urlDbClient
				.getNewSearchRequest("urlSearch");
		try {
			searchRequest.addFilter("url:\"" + url.toExternalForm() + "\"");
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setQueryString("*:*");
		Result result = urlDbClient.search(searchRequest);
		if (result.getDocumentCount() == 0)
			return null;
		return new UrlItem(result.getDocument(0));

	}

	@Override
	public void getNewUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		SearchRequest searchRequest = urlDbClient
				.getNewSearchRequest("urlSearch");
		try {
			searchRequest.addFilter("host:\""
					+ SearchRequest.escapeQuery(host.getName()) + "\"");
			searchRequest.setQueryString("*:*");
			filterQueryToFetchNew(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setRows((int) limit);
		Result result = urlDbClient.search(searchRequest);
		for (ResultDocument item : result)
			urlList.add(new UrlItem(item));
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
		try {
			SearchRequest searchRequest = urlDbClient
					.getNewSearchRequest(urlSearchTemplate.name());
			StringBuffer query = new StringBuffer();
			if (like != null) {
				like = like.trim();
				if (like.length() > 0) {
					like = SearchRequest.escapeQuery(like,
							SearchRequest.RANGE_CHARS);
					like = SearchRequest.escapeQuery(like,
							SearchRequest.AND_OR_NOT_CHARS);
					like = SearchRequest.escapeQuery(like,
							SearchRequest.CONTROL_CHARS);
					Field.URL.addQuery(query, like);
				}
			}
			if (host != null) {
				host = host.trim();
				if (host.length() > 0)
					if (includingSubDomain)
						Field.SUBHOST.addFilterQuery(searchRequest,
								SearchRequest.escapeQuery(host));
					else
						Field.HOST.addFilterQuery(searchRequest,
								SearchRequest.escapeQuery(host));
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					Field.LANG.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(lang));
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					Field.LANGMETHOD.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod));
			}
			if (contentBaseType != null) {
				contentBaseType = contentBaseType.trim();
				if (contentBaseType.length() > 0)
					Field.CONTENTBASETYPE.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentBaseType));
			}
			if (contentTypeCharset != null) {
				contentTypeCharset = contentTypeCharset.trim();
				if (contentTypeCharset.length() > 0)
					Field.CONTENTTYPECHARSET.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentTypeCharset));
			}
			if (contentEncoding != null) {
				contentEncoding = contentEncoding.trim();
				if (contentEncoding.length() > 0)
					Field.CONTENTENCODING.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentEncoding));
			}

			if (robotsTxtStatus != null
					&& robotsTxtStatus != RobotsTxtStatus.ALL)
				Field.ROBOTSTXTSTATUS.addFilterQuery(searchRequest,
						robotsTxtStatus.value);
			if (responseCode != null)
				Field.RESPONSECODE.addFilterQuery(searchRequest, responseCode);
			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				Field.FETCHSTATUS.addFilterQuery(searchRequest,
						fetchStatus.value);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				Field.PARSERSTATUS.addFilterQuery(searchRequest,
						parserStatus.value);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				Field.INDEXSTATUS.addFilterQuery(searchRequest,
						indexStatus.value);

			if (minContentLength != null || maxContentLength != null) {
				String from, to;
				DecimalFormat df = UrlItem.getContentLengthFormat();
				if (minContentLength == null)
					from = df.format(0);
				else
					from = df.format(minContentLength);
				if (maxContentLength == null)
					to = df.format(Integer.MAX_VALUE);
				else
					to = df.format(maxContentLength);
				Field.CONTENTLENGTH.addQueryRange(query, from, to);
			}

			if (startDate != null || endDate != null) {
				String from, to;
				SimpleDateFormat df = UrlItem.getWhenDateFormat();
				if (startDate == null)
					from = "00000000000000";
				else
					from = df.format(startDate);
				if (endDate == null)
					to = "99999999999999";
				else
					to = df.format(endDate);
				Field.WHEN.addFilterRange(searchRequest, from, to);
			}

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public long getUrls(SearchRequest searchRequest, Field orderBy,
			boolean orderAsc, long start, long rows, List<UrlItem> list)
			throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				searchRequest.addSort(orderBy.name, !orderAsc);
			Result result = urlDbClient.search(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(new UrlItem(doc));
			return result.getNumFound();
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void reload(boolean optimize) throws SearchLibException {
		if (optimize) {
			urlDbClient.reload();
			urlDbClient.getIndex().optimize();
			targetClient.reload();
			targetClient.getIndex().optimize();
		}
		urlDbClient.reload();
		targetClient.reload();
	}

	public void updateUrlItem(UrlItem urlItem) throws SearchLibException {
		try {
			IndexDocument indexDocument = new IndexDocument();
			urlItem.populate(indexDocument);
			urlDbClient.updateDocument(indexDocument);
		} catch (NoSuchAlgorithmException e) {
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

	public void updateCrawls(List<Crawl> crawls) throws SearchLibException {
		try {
			if (crawls == null)
				return;
			// Update target index
			List<IndexDocument> documentsToUpdate = new ArrayList<IndexDocument>(
					crawls.size());
			List<String> documentsToDelete = new ArrayList<String>(
					crawls.size());
			for (Crawl crawl : crawls) {
				if (crawl == null)
					continue;
				IndexDocument indexDocument = crawl.getTargetIndexDocument();
				TargetStatus targetStatus = crawl.getUrlItem()
						.getTargetResult();
				if (targetStatus == TargetStatus.TARGET_UPDATE)
					documentsToUpdate.add(indexDocument);
				else if (targetStatus == TargetStatus.TARGET_DELETE)
					documentsToDelete.add(crawl.getUrlItem().getUrl());
			}
			if (documentsToUpdate.size() > 0)
				targetClient.updateDocuments(documentsToUpdate);
			if (documentsToDelete.size() > 0)
				targetClient.deleteDocuments(documentsToDelete);

			// Update URL DB
			documentsToUpdate.clear();
			for (Crawl crawl : crawls) {
				if (crawl == null)
					continue;
				IndexDocument indexDocument = new IndexDocument();
				crawl.getUrlItem().populate(indexDocument);
				documentsToUpdate.add(indexDocument);
			}
			urlDbClient.updateDocuments(documentsToUpdate);

		} catch (NoSuchAlgorithmException e) {
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
			if (documents.size() > 0)
				urlDbClient.updateDocuments(documents);
		} catch (NoSuchAlgorithmException e) {
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
}