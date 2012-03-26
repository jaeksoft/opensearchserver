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
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.http.HttpException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.database.InjectUrlItem.Status;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
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
import com.jaeksoft.searchlib.util.XmlWriter;

public class UrlManager {

	protected Client urlDbClient;

	public enum SearchTemplate {
		urlSearch, urlExport;
	}

	protected Client targetClient = null;

	protected final UrlItemFieldEnum urlItemFieldEnum;

	public UrlManager() {
		urlItemFieldEnum = getNewUrlItemFieldEnum();
	}

	public void init(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		this.urlDbClient = new Client(dataDir, "/url_config.xml", true);
		targetClient = client;
	}

	public void free() {
		this.urlDbClient.close();
	}

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

	public UrlItem getNewUrlItem() {
		return new UrlItem();
	}

	public UrlItemFieldEnum getNewUrlItemFieldEnum() {
		return new UrlItemFieldEnum();
	}

	public boolean exists(String sUrl) throws SearchLibException {
		SearchRequest request = (SearchRequest) urlDbClient
				.getNewRequest(SearchTemplate.urlExport.name());
		request.setQueryString("url:\"" + sUrl + '"');
		return (getUrlList(request, 0, 0, null) > 0);
	}

	public void removeExisting(List<LinkItem> linkList)
			throws SearchLibException {
		Iterator<LinkItem> it = linkList.iterator();
		while (it.hasNext())
			if (exists(it.next().getUrl()))
				it.remove();
	}

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

	private void getFacetLimit(ItemField field, SearchRequest searchRequest,
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

	public void getStartingWith(String queryString, ItemField field,
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

	public final UrlItemFieldEnum getUrlItemFieldEnum() {
		return urlItemFieldEnum;
	}

	public final UrlItem getNewUrlItem(LinkItem linkItem) {
		UrlItem ui = getNewUrlItem();
		ui.setUrl(linkItem.getUrl());
		ui.setParentUrl(linkItem.getParentUrl());
		ui.setOrigin(linkItem.getOrigin());
		return ui;
	}

	final protected UrlItem getNewUrlItem(ResultDocument item) {
		UrlItem ui = getNewUrlItem();
		ui.init(item, urlItemFieldEnum);
		return ui;
	}

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

	public long getSize() throws SearchLibException {
		try {
			return urlDbClient.getIndex().getStatistics().getNumDocs();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

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

	public SearchRequest getSearchRequest(SearchTemplate urlSearchTemplate)
			throws SearchLibException {
		return (SearchRequest) urlDbClient.getNewRequest(urlSearchTemplate
				.name());
	}

	public SearchRequest getSearchRequest(SearchTemplate urlSearchTemplate,
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
								SearchRequest.escapeQuery(host), false, false);
					else
						urlItemFieldEnum.host.addFilterQuery(searchRequest,
								SearchRequest.escapeQuery(host), false, false);
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					urlItemFieldEnum.lang.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(lang), false, false);
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					urlItemFieldEnum.langMethod.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod), true, false);
			}
			if (contentBaseType != null) {
				contentBaseType = contentBaseType.trim();
				if (contentBaseType.length() > 0)
					urlItemFieldEnum.contentBaseType.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentBaseType), true,
							false);
			}
			if (contentTypeCharset != null) {
				contentTypeCharset = contentTypeCharset.trim();
				if (contentTypeCharset.length() > 0)
					urlItemFieldEnum.contentTypeCharset.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentTypeCharset),
							false, false);
			}
			if (contentEncoding != null) {
				contentEncoding = contentEncoding.trim();
				if (contentEncoding.length() > 0)
					urlItemFieldEnum.contentEncoding.addFilterQuery(
							searchRequest,
							SearchRequest.escapeQuery(contentEncoding), true,
							false);
			}

			if (robotsTxtStatus != null
					&& robotsTxtStatus != RobotsTxtStatus.ALL)
				urlItemFieldEnum.robotsTxtStatus.addFilterQuery(searchRequest,
						robotsTxtStatus.value, false, false);
			if (responseCode != null)
				urlItemFieldEnum.responseCode.addFilterQuery(searchRequest,
						responseCode, false, false);
			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				urlItemFieldEnum.fetchStatus.addFilterQuery(searchRequest,
						fetchStatus.value, false, false);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				urlItemFieldEnum.parserStatus.addFilterQuery(searchRequest,
						parserStatus.value, false, false);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				urlItemFieldEnum.indexStatus.addFilterQuery(searchRequest,
						indexStatus.value, false, false);

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
						false, false);
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
						from, to, false, false);
			}

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public long getUrlList(SearchRequest searchRequest, long start, long rows,
			List<UrlItem> list) throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
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

	// TODO : can be mutualised
	public Date getPastDate(long fetchInterval, String intervalUnit) {
		long l;
		if ("hours".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 3600;
		else if ("minutes".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 60;
		else
			// Default is days
			l = fetchInterval * 1000 * 86400;
		return new Date(System.currentTimeMillis() - l);
	}

	/**
	 * Update the targeted index with crawl results
	 * 
	 * @param crawls
	 * @throws SearchLibException
	 */
	public void updateCrawlTarget(List<Crawl> crawls) throws SearchLibException {
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
				if (crawl.getHostUrlList().getListType() == ListType.DBCRAWL)
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
			if (documentsToDelete.size() > 0) {
				targetClient.deleteDocuments(documentsToDelete);
				targetClient.getScreenshotManager().delete(documentsToDelete);
			}
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

	public File exportURLs(SearchRequest searchRequest)
			throws SearchLibException {
		PrintWriter pw = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("OSS_web_crawler_URLs", ".txt");
			pw = new PrintWriter(tempFile);
			int currentPos = 0;
			List<UrlItem> uList = new ArrayList<UrlItem>();
			for (;;) {
				int totalSize;
				totalSize = (int) getUrlList(searchRequest, currentPos, 1000,
						uList);
				for (UrlItem u : uList)
					pw.println(u.getUrl());
				if (uList.size() == 0)
					break;
				uList.clear();
				currentPos += 1000;
				if (currentPos >= totalSize)
					break;
			}
			pw.close();
			pw = null;

		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			if (pw != null)
				pw.close();
		}
		return tempFile;
	}

	public File exportSiteMap(SearchRequest searchRequest)
			throws SearchLibException {
		PrintWriter pw = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("OSS_web_crawler_URLs", ".xml");
			pw = new PrintWriter(tempFile);
			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("urlset", "xmlns",
					"http://www.sitemaps.org/schemas/sitemap/0.9");
			int currentPos = 0;
			List<UrlItem> uList = new ArrayList<UrlItem>();
			for (;;) {
				int totalSize = (int) getUrlList(searchRequest, currentPos,
						1000, uList);
				for (UrlItem u : uList) {
					xmlWriter.startElement("url");
					xmlWriter.writeSubTextNodeIfAny("loc", u.getUrl());
					if (u.getLastModifiedDate() != null)
						xmlWriter.writeSubTextNodeIfAny("lastmod",
								dateformat.format(u.getLastModifiedDate()));
					xmlWriter.endElement();
				}
				if (uList.size() == 0)
					break;
				uList.clear();
				currentPos += 1000;
				if (currentPos >= totalSize)
					break;
			}
			xmlWriter.endElement();
			xmlWriter.endDocument();
			pw.close();
			pw = null;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} finally {
			if (pw != null)
				pw.close();
		}
		return tempFile;
	}

	public int deleteUrls(SearchRequest searchRequest)
			throws SearchLibException {
		try {
			int totalCount = 0;
			long previousNumFound = 0;
			List<UrlItem> urlItemList = new ArrayList<UrlItem>();
			for (;;) {
				long numFound = getUrlList(searchRequest, 0, 1000, urlItemList);
				if (urlItemList.size() == 0)
					break;
				List<String> urlList = new ArrayList<String>(urlItemList.size());
				for (UrlItem urlItem : urlItemList)
					urlList.add(urlItem.getUrl());
				int count = urlDbClient.deleteDocuments(urlList);
				if (count == 0 || previousNumFound == numFound) {
					Logging.error("Bad count at URL deletion " + count + "/"
							+ previousNumFound + "/" + numFound);
					break;
				}
				previousNumFound = numFound;
				totalCount += count;
			}
			return totalCount;
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

	public int updateFetchStatus(SearchRequest searchRequest,
			FetchStatus fetchStatus) throws SearchLibException {
		try {
			int totalCount = 0;
			long previousNumFound = 0;
			urlItemFieldEnum.fetchStatus.addFilterQuery(searchRequest,
					fetchStatus.name, false, true);
			List<UrlItem> urlItemList = new ArrayList<UrlItem>();
			for (;;) {
				long numFound = (int) getUrlList(searchRequest, 0, 1000,
						urlItemList);
				if (urlItemList.size() == 0)
					break;
				for (UrlItem urlItem : urlItemList)
					urlItem.setFetchStatus(fetchStatus);
				updateUrlItems(urlItemList);
				if (previousNumFound == numFound) {
					Logging.error("Bad count at URL fetch status update "
							+ previousNumFound + "/" + numFound);
					break;
				}
				previousNumFound = numFound;
				totalCount += urlItemList.size();
			}
			return totalCount;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	/**
	 * Update the URL database with crawl results
	 * 
	 * @param crawls
	 * @throws SearchLibException
	 */
	public void updateCrawlUrlDb(List<Crawl> crawls) throws SearchLibException {
		if (crawls == null)
			return;
		List<UrlItem> urlItems = new ArrayList<UrlItem>();
		for (Crawl crawl : crawls) {
			if (crawl == null)
				continue;
			urlItems.add(crawl.getUrlItem());
		}
		updateUrlItems(urlItems);
	}

}