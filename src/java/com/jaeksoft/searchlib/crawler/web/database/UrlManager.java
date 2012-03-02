/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.InjectUrlItem.Status;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;

public class UrlManager extends UrlManagerAbstract {

	protected Client urlDbClient;

	@Override
	public void init(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		this.urlDbClient = new Client(dataDir, "/url_config.xml", true);
		targetClient = client;
	}

	@Override
	public void free() {
		this.urlDbClient.close();
	}

	@Override
	public Client getUrlDbClient() {
		return urlDbClient;
	}

	public void deleteUrl(String sUrl) throws SearchLibException {
		try {
			if (sUrl == null)
				return;
			targetClient.deleteDocument(sUrl);
			urlDbClient.deleteDocument(sUrl);
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
	public UrlItem getNewUrlItem() {
		return new UrlItem();
	}

	@Override
	public UrlItemFieldEnum getNewUrlItemFieldEnum() {
		return new UrlItemFieldEnum();
	}

	@Override
	public boolean exists(String sUrl) throws SearchLibException {
		SearchRequest request = getUrlSearchRequest();
		request.setQueryString("url:\"" + sUrl + '"');
		return (getUrls(request, null, false, 0, 0, null) > 0);
	}

	@Override
	public void removeExisting(List<LinkItem> linkList)
			throws SearchLibException {
		Iterator<LinkItem> it = linkList.iterator();
		while (it.hasNext())
			if (exists(it.next().getUrl()))
				it.remove();
	}

	@Override
	public void inject(List<InjectUrlItem> list) throws SearchLibException {
		synchronized (this) {
			try {
				List<IndexDocument> injectList = new ArrayList<IndexDocument>();
				for (InjectUrlItem item : list) {
					if (exists(item.getUrl()))
						item.setStatus(InjectUrlItem.Status.ALREADY);
					else
						injectList.add(item.getIndexDocument(urlItemFieldEnum));
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
		request.addFilter(query.toString(), false);
	}

	private void filterQueryToFetchNew(SearchRequest request,
			Date fetchIntervalDate) throws ParseException {
		StringBuffer query = new StringBuffer();
		query.append("fetchStatus:");
		query.append(FetchStatus.UN_FETCHED.value);
		request.addFilter(query.toString(), false);
		query = new StringBuffer();
		query.append("when:[");
		query.append(UrlItem.getWhenDateFormat().format(fetchIntervalDate));
		query.append(" TO 99999999999999]");
		request.addFilter(query.toString(), false);
	}

	private void getFacetLimit(UrlItemField field, SearchRequest searchRequest,
			int limit, List<NamedItem> list) throws SearchLibException {
		AbstractResultSearch result = (AbstractResultSearch) urlDbClient
				.request(searchRequest);
		Facet facet = result.getFacetList().getByField(field.getName());
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
		SearchRequest searchRequest = new SearchRequest(urlDbClient);
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.getFacetFieldList().add(
				new FacetField("host", 1, false, false));
		return searchRequest;
	}

	private SearchRequest getUrlSearchRequest() throws SearchLibException {
		SearchRequest searchRequest = new SearchRequest(urlDbClient);
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
		getFacetLimit(urlItemFieldEnum.host, searchRequest, limit, hostList);
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
		getFacetLimit(urlItemFieldEnum.host, searchRequest, limit, hostList);
	}

	public void getStartingWith(String queryString, UrlItemField field,
			String start, int limit, List<NamedItem> list)
			throws ParseException, IOException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {
		SearchRequest searchRequest = (SearchRequest) urlDbClient
				.getNewRequest(field + "Facet");
		searchRequest.setQueryString(queryString);
		searchRequest.getFilterList().add(field + ":" + start + "*", false,
				Source.REQUEST);
		getFacetLimit(field, searchRequest, limit, list);
	}

	@Override
	public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		SearchRequest searchRequest = (SearchRequest) urlDbClient
				.getNewRequest("urlSearch");
		try {
			searchRequest.addFilter(
					"host:\"" + SearchRequest.escapeQuery(host.getName())
							+ "\"", false);
			searchRequest.setQueryString("*:*");
			filterQueryToFetchOld(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setRows((int) limit);
		AbstractResultSearch result = (AbstractResultSearch) urlDbClient
				.request(searchRequest);
		for (ResultDocument item : result.getDocuments())
			urlList.add(getNewUrlItem(item));
	}

	@Override
	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		SearchRequest searchRequest = (SearchRequest) urlDbClient
				.getNewRequest("urlSearch");
		try {
			searchRequest.addFilter("url:\"" + url.toExternalForm() + "\"",
					false);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setQueryString("*:*");
		AbstractResultSearch result = (AbstractResultSearch) urlDbClient
				.request(searchRequest);
		if (result.getDocumentCount() == 0)
			return null;
		return getNewUrlItem(result.getDocument(0));

	}

	@Override
	public long getSize() throws SearchLibException {
		try {
			return urlDbClient.getIndex().getStatistics().getNumDocs();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void getNewUrlToFetch(NamedItem host, Date fetchIntervalDate,
			long limit, List<UrlItem> urlList) throws SearchLibException {
		SearchRequest searchRequest = (SearchRequest) urlDbClient
				.getNewRequest("urlSearch");
		try {
			searchRequest.addFilter(
					"host:\"" + SearchRequest.escapeQuery(host.getName())
							+ "\"", false);
			searchRequest.setQueryString("*:*");
			filterQueryToFetchNew(searchRequest, fetchIntervalDate);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setRows((int) limit);
		AbstractResultSearch result = (AbstractResultSearch) urlDbClient
				.request(searchRequest);
		for (ResultDocument item : result.getDocuments())
			urlList.add(getNewUrlItem(item));
	}

	private SearchRequest urlQuery(SearchTemplate urlSearchTemplate,
			String like, String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Date startModifiedDate, Date endModifiedDate)
			throws SearchLibException {
		try {
			SearchRequest searchRequest = (SearchRequest) urlDbClient
					.getNewRequest(urlSearchTemplate.name());
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
					urlItemFieldEnum.url.addQuery(query, like, false);
				}
			}
			if (host != null) {
				host = host.trim();
				if (host.length() > 0)
					if (includingSubDomain)
						urlItemFieldEnum.subhost.addFilterQuery(searchRequest,
								SearchRequest.escapeQuery(host), false);
					else
						urlItemFieldEnum.host.addFilterQuery(searchRequest,
								SearchRequest.escapeQuery(host), false);
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					urlItemFieldEnum.lang.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(lang), false);
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					urlItemFieldEnum.langMethod.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod), false);
			}
			if (contentBaseType != null) {
				contentBaseType = contentBaseType.trim();
				if (contentBaseType.length() > 0)
					urlItemFieldEnum.contentBaseType.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentBaseType), false);
			}
			if (contentTypeCharset != null) {
				contentTypeCharset = contentTypeCharset.trim();
				if (contentTypeCharset.length() > 0)
					urlItemFieldEnum.contentTypeCharset.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentTypeCharset),
							false);
			}
			if (contentEncoding != null) {
				contentEncoding = contentEncoding.trim();
				if (contentEncoding.length() > 0)
					urlItemFieldEnum.contentEncoding.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentEncoding), false);
			}

			if (robotsTxtStatus != null
					&& robotsTxtStatus != RobotsTxtStatus.ALL)
				urlItemFieldEnum.robotsTxtStatus.addFilterQuery(searchRequest,
						robotsTxtStatus.value, false);
			if (responseCode != null)
				urlItemFieldEnum.responseCode.addFilterQuery(searchRequest,
						responseCode, false);
			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				urlItemFieldEnum.fetchStatus.addFilterQuery(searchRequest,
						fetchStatus.value, false);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				urlItemFieldEnum.parserStatus.addFilterQuery(searchRequest,
						parserStatus.value, false);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				urlItemFieldEnum.indexStatus.addFilterQuery(searchRequest,
						indexStatus.value, false);

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
				urlItemFieldEnum.contentLength.addQueryRange(query, from, to,
						false);
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
				urlItemFieldEnum.when.addFilterRange(searchRequest, from, to,
						false);
			}

			if (startModifiedDate != null || endModifiedDate != null) {
				String from, to;
				SimpleDateFormat df = UrlItem.getWhenDateFormat();
				if (startModifiedDate == null)
					from = "00000000000000";
				else
					from = df.format(startModifiedDate);
				if (endModifiedDate == null)
					to = "99999999999999";
				else
					to = df.format(endModifiedDate);
				urlItemFieldEnum.lastModifiedDate.addFilterRange(searchRequest,
						from, to, false);
			}

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	private long getUrls(SearchRequest searchRequest, UrlItemField orderBy,
			boolean orderAsc, long start, long rows, List<UrlItem> list)
			throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				searchRequest.addSort(orderBy.getName(), !orderAsc);
			AbstractResultSearch result = (AbstractResultSearch) urlDbClient
					.request(searchRequest);
			if (list != null)
				for (ResultDocument doc : result.getDocuments())
					list.add(getNewUrlItem(doc));
			return result.getNumFound();
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public long getUrls(SearchTemplate urlSearchTemplate, String like,
			String host, boolean includingSubDomain, String lang,
			String langMethod, String contentBaseType,
			String contentTypeCharset, String contentEncoding,
			Integer minContentLength, Integer maxContentLength,
			RobotsTxtStatus robotsTxtStatus, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Date startModifiedDate, Date endModifiedDate, UrlItemField orderBy,
			boolean orderAsc, long start, long rows, List<UrlItem> list)
			throws SearchLibException {
		SearchRequest searchRequest = urlQuery(urlSearchTemplate, like, host,
				includingSubDomain, lang, langMethod, contentBaseType,
				contentTypeCharset, contentEncoding, minContentLength,
				maxContentLength, robotsTxtStatus, fetchStatus, responseCode,
				parserStatus, indexStatus, startDate, endDate,
				startModifiedDate, endModifiedDate);
		return getUrls(searchRequest, orderBy, orderAsc, start, rows, list);
	}

	@Override
	public void reload(boolean optimize) throws SearchLibException {
		if (optimize) {
			urlDbClient.reload();
			urlDbClient.getIndex().optimize();
		}
		targetClient.reload();
	}

	public void updateUrlItem(UrlItem urlItem) throws SearchLibException {
		try {
			IndexDocument indexDocument = new IndexDocument();
			urlItem.populate(indexDocument, urlItemFieldEnum);
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
				urlItem.populate(indexDocument, urlItemFieldEnum);
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