/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.ItemField;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.crawler.common.database.AbstractManager;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem.Origin;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapUrl;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class UrlManager extends AbstractManager {

	public static enum SearchTemplate {
		urlSearch, urlExport, hostFacet;
	}

	public UrlManager() {
	}

	public void init(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "web_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();
		Client dbClient = new Client(dataDir,
				"/com/jaeksoft/searchlib/url_config.xml", true);
		super.init(client, dbClient);
	}

	public void deleteUrls(Collection<String> workDeleteUrlList)
			throws SearchLibException {
		String targetField = findIndexedFieldOfTargetIndex(
				targetClient.getWebCrawlerFieldMap(),
				UrlItemFieldEnum.INSTANCE.url.getName());
		if (targetField != null)
			targetClient.deleteDocuments(targetField, workDeleteUrlList);
		dbClient.deleteDocuments(UrlItemFieldEnum.INSTANCE.url.getName(),
				workDeleteUrlList);
	}

	public boolean exists(String sUrl) throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) dbClient
				.getNewRequest(SearchTemplate.urlExport.name());
		request.getReturnFieldList().clear();
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

	public void inject(List<String> urls, InfoCallback infoCallback)
			throws SearchLibException {
		try {
			int already = 0;
			int injected = 0;
			List<IndexDocument> injectList = new ArrayList<IndexDocument>(0);
			for (String url : urls) {
				if (exists(url))
					already++;
				else {
					UrlItem item = getNewUrlItem(url);
					IndexDocument indexDocument = new IndexDocument();
					item.populate(indexDocument);
					injectList.add(indexDocument);
				}
			}
			if (injectList.size() > 0) {
				injected = dbClient.updateDocuments(injectList);
				if (injected > 0)
					dbClient.reload();
			}
			if (infoCallback != null)
				infoCallback.setInfo("Injected: " + injected + " - Already: "
						+ already);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public void injectPrefix(List<PatternItem> patternList)
			throws SearchLibException {
		Iterator<PatternItem> it = patternList.iterator();
		List<String> urlList = new ArrayList<String>(0);
		while (it.hasNext()) {
			PatternItem item = it.next();
			if (item.getStatus() == PatternItem.Status.INJECTED) {
				URL url = item.tryExtractURL();
				if (url != null)
					urlList.add(url.toExternalForm());
			}
		}
		inject(urlList, null);
	}

	private void filterQueryToFetch(AbstractSearchRequest request,
			FetchStatus fetchStatus, Date before, Date after)
			throws ParseException {
		if (fetchStatus != null) {
			StringBuilder query = new StringBuilder();
			query.append("fetchStatus:");
			query.append(fetchStatus.value);
			request.addFilter(query.toString(), false);
		}
		if (before != null) {
			StringBuilder query = new StringBuilder();
			query.append("when:[00000000000000 TO ");
			query.append(UrlItem.whenDateFormat.format(before));
			query.append("]");
			request.addFilter(query.toString(), false);
		}
		if (after != null) {
			StringBuilder query = new StringBuilder();
			query.append("when:[");
			query.append(UrlItem.whenDateFormat.format(after));
			query.append(" TO 99999999999999]");
			request.addFilter(query.toString(), false);
		}
	}

	private int getFacetLimit(ItemField field,
			AbstractSearchRequest searchRequest, int urlLimit,
			int maxUrlPerHost, List<NamedItem> list) throws SearchLibException {
		AbstractResultSearch result = (AbstractResultSearch) dbClient
				.request(searchRequest);
		Facet facet = result.getFacetList().getByField(field.getName());
		for (FacetItem facetItem : facet) {
			if (urlLimit <= 0)
				break;
			int nbURL = facetItem.getCount();
			if (nbURL == 0)
				continue;
			urlLimit -= nbURL > maxUrlPerHost ? maxUrlPerHost : nbURL;
			if (urlLimit < 0)
				urlLimit = 0;
			String term = facetItem.getTerm();
			if (term == null)
				continue;
			if (term.length() == 0)
				continue;
			synchronized (list) {
				list.add(new NamedItem(term, facetItem.getCount()));
			}
		}
		return urlLimit;
	}

	private AbstractSearchRequest getHostFacetSearchRequest() {
		AbstractSearchRequest searchRequest = new SearchPatternRequest(dbClient);
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.getFacetFieldList().put(
				new FacetField("host", 1, false, false, null));
		return searchRequest;
	}

	public int getHostToFetch(FetchStatus fetchStatus, Date before, Date after,
			int urlLimit, int maxUrlPerHost, List<NamedItem> hostList)
			throws SearchLibException {
		AbstractSearchRequest searchRequest = getHostFacetSearchRequest();
		searchRequest.setEmptyReturnsAll(true);
		try {
			filterQueryToFetch(searchRequest, fetchStatus, before, after);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		return getFacetLimit(UrlItemFieldEnum.INSTANCE.host, searchRequest,
				urlLimit, maxUrlPerHost, hostList);
	}

	public void getStartingWith(String queryString, ItemField field,
			String start, int urlLimit, int maxUrlPerHost, List<NamedItem> list)
			throws ParseException, IOException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
				.getNewRequest(field + "Facet");
		searchRequest.setQueryString(queryString);
		searchRequest.getFilterList().add(
				new QueryFilter(field + ":" + start + "*", false,
						FilterAbstract.Source.REQUEST, null));
		getFacetLimit(field, searchRequest, urlLimit, maxUrlPerHost, list);
	}

	public final UrlItem getNewUrlItem(LinkItem linkItem) {
		UrlItem ui = new UrlItem();
		ui.setUrl(linkItem.getUrl());
		ui.setParentUrl(linkItem.getParentUrl());
		ui.setOrigin(linkItem.getOrigin());
		return ui;
	}

	final protected UrlItem getNewUrlItem(ResultDocument item) {
		UrlItem ui = new UrlItem();
		ui.init(item);
		return ui;
	}

	final protected UrlItem getNewUrlItem(SiteMapUrl siteMapUrl) {
		UrlItem ui = new UrlItem();
		ui.setUrl(siteMapUrl.getLoc().toString());
		ui.setOrigin(Origin.sitemap);
		return ui;
	}

	public final UrlItem getNewUrlItem(String url) {
		UrlItem ui = new UrlItem();
		ui.setUrl(url);
		ui.setOrigin(Origin.manual);
		return ui;
	}

	public void getUrlToFetch(NamedItem host, FetchStatus fetchStatus,
			Date before, Date after, long urlLimit, List<UrlItem> urlList)
			throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
				.getNewRequest("urlSearch");
		try {
			searchRequest.addFilter(
					"host:\"" + QueryUtils.escapeQuery(host.getName()) + "\"",
					false);
			searchRequest.setEmptyReturnsAll(true);
			filterQueryToFetch(searchRequest, fetchStatus, before, after);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
		searchRequest.setRows((int) urlLimit);
		AbstractResultSearch result = (AbstractResultSearch) dbClient
				.request(searchRequest);
		for (ResultDocument item : result)
			urlList.add(getNewUrlItem(item));
	}

	public UrlItem getUrlToFetch(URL url) throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
				.getNewRequest("urlSearch");
		return getUrl(searchRequest, url.toExternalForm());
	}

	public AbstractSearchRequest getSearchRequest(
			SearchTemplate urlSearchTemplate) throws SearchLibException {
		return (AbstractSearchRequest) dbClient.getNewRequest(urlSearchTemplate
				.name());
	}

	public int countBackLinks(String url) throws SearchLibException {
		try {
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
					.getNewRequest("urlExport");
			StringBuilder sb = new StringBuilder();
			UrlItemFieldEnum.INSTANCE.inlink.addQuery(sb, url, true);
			sb.append(" OR");
			UrlItemFieldEnum.INSTANCE.outlink.addQuery(sb, url, true);
			UrlItemFieldEnum.INSTANCE.parserStatus.addFilterQuery(
					searchRequest, ParserStatus.PARSED.value, false, false);
			searchRequest.setQueryString(sb.toString());
			searchRequest.setRows(0);
			AbstractResultSearch result = (AbstractResultSearch) dbClient
					.request(searchRequest);
			return result.getNumFound();
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public AbstractSearchRequest getSearchRequest(
			SearchTemplate urlSearchTemplate, String like, String host,
			boolean includingSubDomain, String lang, String langMethod,
			String contentBaseType, String contentTypeCharset,
			String contentEncoding, Integer minContentLength,
			Integer maxContentLength, RobotsTxtStatus robotsTxtStatus,
			FetchStatus fetchStatus, Integer responseCode,
			ParserStatus parserStatus, IndexStatus indexStatus, Date startDate,
			Date endDate, Date startModifiedDate, Date endModifiedDate)
			throws SearchLibException {
		try {
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
					.getNewRequest(urlSearchTemplate.name());
			StringBuilder query = new StringBuilder();
			if (like != null) {
				like = like.trim();
				if (like.length() > 0) {
					like = QueryUtils.escapeQuery(like,
							QueryUtils.CONTROL_CHARS);
					like = QueryUtils.escapeQuery(like, QueryUtils.RANGE_CHARS);
					like = QueryUtils.escapeQuery(like,
							QueryUtils.AND_OR_NOT_CHARS);
					UrlItemFieldEnum.INSTANCE.url.addQuery(query, like, false);
				}
			}
			if (host != null) {
				host = host.trim();
				if (host.length() > 0)
					if (includingSubDomain)
						UrlItemFieldEnum.INSTANCE.subhost.addFilterQuery(
								searchRequest, QueryUtils.escapeQuery(host),
								false, false);
					else
						UrlItemFieldEnum.INSTANCE.host.addFilterQuery(
								searchRequest, QueryUtils.escapeQuery(host),
								false, false);
			}
			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					UrlItemFieldEnum.INSTANCE.lang.addFilterQuery(
							searchRequest, QueryUtils.escapeQuery(lang), false,
							false);
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					UrlItemFieldEnum.INSTANCE.langMethod.addFilterQuery(
							searchRequest, QueryUtils.escapeQuery(langMethod),
							true, false);
			}
			if (contentBaseType != null) {
				contentBaseType = contentBaseType.trim();
				if (contentBaseType.length() > 0)
					UrlItemFieldEnum.INSTANCE.contentBaseType.addFilterQuery(
							searchRequest,
							QueryUtils.escapeQuery(contentBaseType), true,
							false);
			}
			if (contentTypeCharset != null) {
				contentTypeCharset = contentTypeCharset.trim();
				if (contentTypeCharset.length() > 0)
					UrlItemFieldEnum.INSTANCE.contentTypeCharset
							.addFilterQuery(searchRequest,
									QueryUtils.escapeQuery(contentTypeCharset),
									false, false);
			}
			if (contentEncoding != null) {
				contentEncoding = contentEncoding.trim();
				if (contentEncoding.length() > 0)
					UrlItemFieldEnum.INSTANCE.contentEncoding.addFilterQuery(
							searchRequest,
							QueryUtils.escapeQuery(contentEncoding), true,
							false);
			}

			if (robotsTxtStatus != null
					&& robotsTxtStatus != RobotsTxtStatus.ALL)
				UrlItemFieldEnum.INSTANCE.robotsTxtStatus.addFilterQuery(
						searchRequest, robotsTxtStatus.value, false, false);
			if (responseCode != null)
				UrlItemFieldEnum.INSTANCE.responseCode.addFilterQuery(
						searchRequest, responseCode, false, false);
			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				UrlItemFieldEnum.INSTANCE.fetchStatus.addFilterQuery(
						searchRequest, fetchStatus.value, false, false);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				UrlItemFieldEnum.INSTANCE.parserStatus.addFilterQuery(
						searchRequest, parserStatus.value, false, false);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				UrlItemFieldEnum.INSTANCE.indexStatus.addFilterQuery(
						searchRequest, indexStatus.value, false, false);

			if (minContentLength != null || maxContentLength != null) {
				String from, to;
				if (minContentLength == null)
					from = UrlItem.longFormat.format(0);
				else
					from = UrlItem.longFormat.format(minContentLength);
				if (maxContentLength == null)
					to = UrlItem.longFormat.format(Integer.MAX_VALUE);
				else
					to = UrlItem.longFormat.format(maxContentLength);
				UrlItemFieldEnum.INSTANCE.contentLength.addQueryRange(query,
						from, to, false);
			}

			if (startDate != null || endDate != null) {
				String from, to;
				if (startDate == null)
					from = "00000000000000";
				else
					from = UrlItem.whenDateFormat.format(startDate);
				if (endDate == null)
					to = "99999999999999";
				else
					to = UrlItem.whenDateFormat.format(endDate);
				UrlItemFieldEnum.INSTANCE.when.addFilterRange(searchRequest,
						from, to, false, false);
			}

			if (startModifiedDate != null || endModifiedDate != null) {
				String from, to;
				if (startModifiedDate == null)
					from = "00000000000000";
				else
					from = UrlItem.whenDateFormat.format(startModifiedDate);
				if (endModifiedDate == null)
					to = "99999999999999";
				else
					to = UrlItem.whenDateFormat.format(endModifiedDate);
				UrlItemFieldEnum.INSTANCE.lastModifiedDate.addFilterRange(
						searchRequest, from, to, false, false);
			}

			searchRequest.setEmptyReturnsAll(true);
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	private UrlItem getUrl(AbstractSearchRequest request, String sUrl)
			throws SearchLibException {
		if (request == null)
			request = (AbstractSearchRequest) dbClient
					.getNewRequest(SearchTemplate.urlSearch.name());
		else
			request.reset();
		request.setQueryString("url:\"" + QueryUtils.escapeQuery(sUrl) + '"');
		request.setStart(0);
		request.setRows(1);
		try {
			AbstractResultSearch result = (AbstractResultSearch) dbClient
					.request(request);
			for (ResultDocument doc : result)
				return getNewUrlItem(doc);
			return null;
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		}
	}

	public long getUrlList(AbstractSearchRequest searchRequest, long start,
			long rows, List<UrlItem> list) throws SearchLibException {
		searchRequest.reset();
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			AbstractResultSearch result = (AbstractResultSearch) dbClient
					.request(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(getNewUrlItem(doc));
			return result.getNumFound();
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		}
	}

	public Facet getHostFacetList(int minCount) throws SearchLibException {
		try {
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) dbClient
					.getNewRequest(UrlManager.SearchTemplate.hostFacet.name());
			searchRequest.setEmptyReturnsAll(true);
			FacetField facetField = searchRequest.getFacetFieldList().get(
					UrlItemFieldEnum.INSTANCE.host.getName());
			if (minCount < 0)
				minCount = 0;
			facetField.setMinCount(minCount);
			AbstractResultSearch result = (AbstractResultSearch) dbClient
					.request(searchRequest);
			if (result == null)
				return null;
			return result.getFacetList().getByField(
					UrlItemFieldEnum.INSTANCE.host.getName());
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		}
	}

	public void updateUrlItem(UrlItem urlItem) throws SearchLibException {
		try {
			IndexDocument indexDocument = new IndexDocument();
			urlItem.populate(indexDocument);
			dbClient.updateDocument(indexDocument);
		} catch (IOException e) {
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
				urlItem.populate(indexDocument);
				documents.add(indexDocument);
			}
			if (documents.size() > 0)
				dbClient.updateDocuments(documents);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	/**
	 * Update the targeted index with crawl results
	 * 
	 * @param crawls
	 * @throws SearchLibException
	 */
	public void updateCrawlTarget(List<Crawl> crawls, boolean propagateDeletion)
			throws SearchLibException {
		try {
			if (crawls == null)
				return;
			// Update target index
			List<IndexDocument> documentsToUpdate = new ArrayList<IndexDocument>(
					crawls.size());
			List<String> documentsToDelete = new ArrayList<String>(
					crawls.size());
			String uniqueField = targetClient.getSchema().getUniqueField();
			for (Crawl crawl : crawls) {
				if (crawl == null)
					continue;
				if (crawl.getHostUrlList().getListType() == ListType.DBCRAWL)
					continue;
				UrlItem currentUrlItem = crawl.getUrlItem();
				List<IndexDocument> indexDocuments = crawl
						.getTargetIndexDocuments();
				TargetStatus targetStatus = currentUrlItem.getIndexStatus().targetStatus;
				if (targetStatus == TargetStatus.TARGET_UPDATE) {
					if (CollectionUtils.isEmpty(indexDocuments)) {
						currentUrlItem
								.setIndexStatus(IndexStatus.NOTHING_TO_INDEX);
						continue;
					}
					for (IndexDocument indexDocument : indexDocuments) {
						if (indexDocument == null)
							continue;
						if (uniqueField != null
								&& !indexDocument.hasContent(uniqueField)) {
							currentUrlItem
									.setIndexStatus(IndexStatus.INDEX_ERROR);
						} else
							documentsToUpdate.add(indexDocument);
					}
				} else if (targetStatus == TargetStatus.TARGET_DELETE)
					documentsToDelete.add(crawl.getUrlItem().getUrl());
			}
			if (documentsToUpdate.size() > 0) {
				targetClient.updateDocuments(documentsToUpdate);
				for (Crawl crawl : crawls) {
					UrlItem currentUrlItem = crawl.getUrlItem();
					IndexStatus indexStatus = currentUrlItem.getIndexStatus();
					if (indexStatus == IndexStatus.TO_INDEX
							|| indexStatus == IndexStatus.NOT_INDEXED)
						currentUrlItem.setIndexStatus(IndexStatus.INDEXED);
				}
			}
			if (documentsToDelete.size() > 0 && propagateDeletion) {
				String targetField = findIndexedFieldOfTargetIndex(
						targetClient.getWebCrawlerFieldMap(),
						UrlItemFieldEnum.INSTANCE.url.getName());
				if (targetField != null)
					targetClient
							.deleteDocuments(targetField, documentsToDelete);
				targetClient.getScreenshotManager().delete(documentsToDelete);
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	public File exportURLs(AbstractSearchRequest searchRequest)
			throws SearchLibException {
		PrintWriter pw = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("OSS_web_crawler_URLs", ".txt");
			pw = new PrintWriter(tempFile);
			int currentPos = 0;
			List<UrlItem> uList = new ArrayList<UrlItem>();
			for (;;) {
				int totalSize = (int) getUrlList(searchRequest, currentPos,
						1000, uList);
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

	public File exportCrawlCache(AbstractSearchRequest searchRequest)
			throws IOException, SearchLibException {
		File tempFile = null;
		ZipArchiveOutputStream zipOutput = null;
		CrawlCacheManager crawlCacheManager = ClientCatalog
				.getCrawlCacheManager();
		if (crawlCacheManager.isDisabled())
			throw new SearchLibException("The crawlCache is disabled.");
		try {
			tempFile = File
					.createTempFile("OSS_web_crawler_crawlcache", ".zip");
			zipOutput = new ZipArchiveOutputStream(tempFile);
			int currentPos = 0;
			List<UrlItem> uList = new ArrayList<UrlItem>();
			for (;;) {
				int totalSize = (int) getUrlList(searchRequest, currentPos,
						1000, uList);
				if (uList.size() == 0)
					break;
				for (UrlItem u : uList) {
					URL url = u.getURL();
					if (url == null)
						continue;
					DownloadItem downloadItem = crawlCacheManager.loadCache(url
							.toURI());
					if (downloadItem == null)
						continue;
					downloadItem.writeToZip(zipOutput);
				}
				uList.clear();
				currentPos += 1000;
				if (currentPos >= totalSize)
					break;
			}
			zipOutput.close();
			zipOutput = null;
			return tempFile;
		} catch (JSONException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} finally {
			if (zipOutput != null)
				IOUtils.closeQuietly(zipOutput);
		}
	}

	public File exportSiteMap(AbstractSearchRequest searchRequest)
			throws SearchLibException {
		PrintWriter pw = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("OSS_web_crawler_URLs", ".xml");
			pw = new PrintWriter(tempFile);
			ThreadSafeDateFormat dateformat = new ThreadSafeSimpleDateFormat(
					"yyyy-MM-dd");
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

	public long deleteUrls(AbstractSearchRequest searchRequest, int bufferSize,
			TaskLog taskLog) throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			long total = 0;
			List<UrlItem> urlItemList = new ArrayList<UrlItem>();
			long last = 0;
			for (;;) {
				urlItemList.clear();
				long len = getUrlList(searchRequest, 0, bufferSize, urlItemList);
				if (urlItemList.size() == 0)
					break;
				if (len == last) {
					Logging.warn("URLManager loop redundancy (deleteUrls): "
							+ len + "/" + total);
					break;
				}
				last = len;
				List<String> urlList = new ArrayList<String>(urlItemList.size());
				for (UrlItem urlItem : urlItemList)
					urlList.add(urlItem.getUrl());
				dbClient.deleteDocuments(
						UrlItemFieldEnum.INSTANCE.url.getName(), urlList);
				total += urlItemList.size();
				taskLog.setInfo(total + " URL(s) deleted");
				if (taskLog.isAbortRequested())
					throw new SearchLibException.AbortException();
				ThreadUtils.sleepMs(100);
			}
			return total;
		} finally {
			resetCurrentTaskLog();
		}
	}

	public long synchronizeIndex(AbstractSearchRequest searchRequest,
			int bufferSize, TaskLog taskLog) throws SearchLibException {
		String targetField = findIndexedFieldOfTargetIndex(
				targetClient.getWebCrawlerFieldMap(),
				UrlItemFieldEnum.INSTANCE.url.getName());
		return synchronizeIndex(searchRequest, targetField,
				UrlItemFieldEnum.INSTANCE.url.getName(), bufferSize, taskLog);
	}

	public long updateFetchStatus(AbstractSearchRequest searchRequest,
			FetchStatus fetchStatus, int bufferSize, TaskLog taskLog)
			throws SearchLibException, IOException {
		setCurrentTaskLog(taskLog);
		try {
			long total = 0;
			UrlItemFieldEnum.INSTANCE.fetchStatus.addFilterQuery(searchRequest,
					fetchStatus.value, false, true);
			List<UrlItem> urlItemList = new ArrayList<UrlItem>();
			long last = 0;
			for (;;) {
				urlItemList.clear();
				long len = getUrlList(searchRequest, 0, bufferSize, urlItemList);
				if (urlItemList.size() == 0)
					break;
				if (len == last) {
					Logging.warn("URLManager loop redundancy (updateFetchStatus): "
							+ len + "/" + total);
					break;
				}
				last = len;
				for (UrlItem urlItem : urlItemList)
					urlItem.setFetchStatus(fetchStatus);
				updateUrlItems(urlItemList);
				total += urlItemList.size();
				taskLog.setInfo(total + " URL(s) updated");
				if (taskLog.isAbortRequested())
					throw new SearchLibException.AbortException();
				ThreadUtils.sleepMs(100);
			}
			return total;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} finally {
			resetCurrentTaskLog();
		}
	}

	public long updateSiteMap(TaskLog taskLog) throws SearchLibException {
		setCurrentTaskLog(taskLog);
		HttpDownloader httpDownloader = null;
		try {
			AbstractSearchRequest request = (AbstractSearchRequest) dbClient
					.getNewRequest(SearchTemplate.urlSearch.name());
			long inserted = 0;
			long existing = 0;
			long setToFetchFirst = 0;
			int everyTen = 0;
			targetClient.getSiteMapList();
			httpDownloader = targetClient.getWebCrawlMaster()
					.getNewHttpDownloader(true);
			Set<SiteMapUrl> siteMapUrlSet = new HashSet<SiteMapUrl>(0);
			List<UrlItem> urlItemList = new ArrayList<UrlItem>(0);
			long now = System.currentTimeMillis();
			for (SiteMapItem siteMapItem : targetClient.getSiteMapList()
					.getArray()) {
				taskLog.setInfo("Loading " + siteMapItem.getUri());
				siteMapUrlSet.clear();
				urlItemList.clear();
				siteMapItem.load(httpDownloader, siteMapUrlSet);
				for (SiteMapUrl siteMapUrl : siteMapUrlSet) {
					UrlItem urlItem = getUrl(request, siteMapUrl.getLoc()
							.toString());
					if (urlItem == null) {
						urlItemList.add(getNewUrlItem(siteMapUrl));
						inserted++;
					} else {
						existing++;
						long timeDistanceMs = now - urlItem.getWhen().getTime();
						FetchStatus fetchStatus = urlItem.getFetchStatus();
						if (fetchStatus == FetchStatus.UN_FETCHED
								|| (fetchStatus == FetchStatus.FETCHED && siteMapUrl
										.getChangeFreq().needUpdate(
												timeDistanceMs))) {
							if (fetchStatus != FetchStatus.FETCH_FIRST) {
								urlItem.setFetchStatus(FetchStatus.FETCH_FIRST);
								urlItemList.add(urlItem);
								setToFetchFirst++;
							}
						}
					}
					if (everyTen == 10) {
						if (taskLog.isAbortRequested())
							throw new SearchLibException.AbortException();
						everyTen = 0;
						taskLog.setInfo(inserted + "/" + existing
								+ " URL(s) inserted/existing");
					} else
						everyTen++;
				}
				if (urlItemList.size() > 0)
					updateUrlItems(urlItemList);
			}
			taskLog.setInfo(inserted + "/" + existing + "/" + setToFetchFirst
					+ " URL(s) inserted/existing/fetchFirst");
			Logging.info(taskLog.getInfo());
			return inserted + existing;
		} finally {
			if (httpDownloader != null)
				httpDownloader.release();
			resetCurrentTaskLog();
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