/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.crawler.web.spider;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheProvider;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.database.CookieManager;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.HeaderItem;
import com.jaeksoft.searchlib.crawler.web.database.HeaderManager;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem.Origin;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.database.pattern.PatternListMatcher;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxtItem;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.HtmlParser;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginList;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Crawl {

	private List<IndexDocument> targetIndexDocuments;
	private HostUrlList hostUrlList;
	private final UrlItem urlItem;
	private final UrlManager urlManager;
	private final UrlFilterItem[] urlFilterList;
	private final CredentialManager credentialManager;
	private final HeaderManager headerManager;
	private final CookieManager cookieManager;
	private final CrawlCacheManager crawlCacheManager;
	private CredentialItem credentialItem;
	private String userAgent;
	private final ParserSelector parserSelector;
	private Config config;
	private Parser parser;
	private String error;
	private final boolean linkDetectionEnabled;
	private List<LinkItem> discoverLinks;
	private FieldMap urlFieldMap;
	private URI redirectUrlLocation;
	private final PatternListMatcher inclusionMatcher;
	private final PatternListMatcher exclusionMatcher;
	private final boolean robotsTxtEnabled;

	public Crawl(HostUrlList hostUrlList, UrlItem urlItem, Config config, ParserSelector parserSelector)
			throws IOException, SearchLibException {
		this.credentialManager = config.getWebCredentialManager();
		this.cookieManager = config.getWebCookieManager();
		this.headerManager = config.getWebHeaderManager();
		this.crawlCacheManager = CrawlCacheManager.getInstance(config);
		this.credentialItem = null;
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		this.hostUrlList = hostUrlList;
		this.targetIndexDocuments = null;
		this.urlFieldMap = config.getWebCrawlerFieldMap();
		this.urlManager = config.getUrlManager();
		this.urlFilterList = config.getUrlFilterList().getArray();
		this.linkDetectionEnabled = propertyManager.getLinkDetectionEnabled().getValue();
		this.discoverLinks = null;
		this.urlItem = urlItem;
		this.urlItem.setWhenNow();
		this.userAgent = propertyManager.getUserAgent().getValue();
		this.parser = null;
		this.parserSelector = parserSelector;
		this.config = config;
		this.error = null;
		this.redirectUrlLocation = null;
		this.exclusionMatcher = propertyManager.getExclusionEnabled().getValue() ?
				config.getExclusionPatternManager().getPatternListMatcher() :
				null;
		this.inclusionMatcher = propertyManager.getInclusionEnabled().getValue() ?
				config.getInclusionPatternManager().getPatternListMatcher() :
				null;
		this.robotsTxtEnabled = propertyManager.getRobotsTxtEnabled().getValue();
	}

	public Crawl(WebCrawlThread crawlThread) throws SearchLibException, IOException {
		this(crawlThread.getHostUrlList(), crawlThread.getCurrentUrlItem(), crawlThread.getConfig(),
				crawlThread.getConfig().getParserSelector());
	}

	protected void parseContent(final InputStream inputStream)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException,
			SearchLibException, NoSuchAlgorithmException, URISyntaxException {
		if (parserSelector == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}
		String fileName = urlItem.getContentDispositionFilename();
		if (fileName == null) {
			URL url = urlItem.getURL();
			if (url != null)
				fileName = FilenameUtils.getName(url.getFile());
		}
		final IndexDocument sourceDocument = new IndexDocument();
		urlItem.populate(sourceDocument);
		Date parserStartDate = new Date();
		// TODO Which language for OCR ?
		parser = parserSelector.parseStream(sourceDocument, fileName, urlItem.getContentBaseType(), urlItem.getUrl(),
				inputStream, null, parserSelector.getWebCrawlerDefaultParser(),
				parserSelector.getFileCrawlerDefaultParser());
		if (parser == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}

		if (parser.getError() != null) {
			urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
			return;
		}
		urlItem.clearInLinks();
		urlItem.clearOutLinks();

		for (ParserResultItem result : parser.getParserResults()) {
			urlItem.addInLinks(result.getFieldContent(ParserFieldEnum.internal_link));
			urlItem.addInLinks(result.getFieldContent(ParserFieldEnum.internal_link_nofollow));
			urlItem.addOutLinks(result.getFieldContent(ParserFieldEnum.external_link));
			urlItem.addOutLinks(result.getFieldContent(ParserFieldEnum.external_link_nofollow));
			urlItem.setLang(result.getFieldValue(ParserFieldEnum.lang, 0));
			urlItem.setLangMethod(result.getFieldValue(ParserFieldEnum.lang_method, 0));
			urlItem.setContentTypeCharset(result.getFieldValue(ParserFieldEnum.charset, 0));
		}
		ParserStatus parsedStatus = ParserStatus.PARSED;
		if (parser instanceof HtmlParser)
			if (!((HtmlParser) parser).isCanonical())
				parsedStatus = ParserStatus.PARSED_NON_CANONICAL;
		urlItem.setParserStatus(parsedStatus);
		String oldMd5size = urlItem.getMd5size();
		String newMd5size = parser.getMd5size();
		urlItem.setMd5size(newMd5size);
		Date oldContentUpdateDate = urlItem.getContentUpdateDate();
		Date newContentUpdateDate = null;
		if (oldContentUpdateDate == null)
			newContentUpdateDate = parserStartDate;
		else {
			if (oldMd5size != null && newMd5size != null)
				if (!oldMd5size.equals(newMd5size))
					newContentUpdateDate = parserStartDate;
		}
		if (newContentUpdateDate != null)
			urlItem.setContentUpdateDate(newContentUpdateDate);

		for (ParserResultItem result : parser.getParserResults()) {
			FieldContent fieldContent = result.getFieldContent(ParserFieldEnum.meta_robots);
			if (fieldContent != null) {
				List<FieldValueItem> fieldValues = fieldContent.getValues();
				if (fieldValues != null) {
					for (FieldValueItem item : result.getFieldContent(ParserFieldEnum.meta_robots).getValues())
						if ("noindex".equalsIgnoreCase(item.getValue())) {
							urlItem.setIndexStatus(IndexStatus.META_NOINDEX);
							break;
						}
				}
			}
		}
	}

	public boolean checkRobotTxtAllow(HttpDownloader httpDownloader)
			throws SearchLibException, URISyntaxException, ClassNotFoundException, IOException {
		RobotsTxtStatus robotsTxtStatus;
		if (robotsTxtEnabled) {
			RobotsTxtItem robotsTxt = config.getRobotsTxtCache().getRobotsTxt(httpDownloader, config, urlItem.getURL(),
					false);
			robotsTxtStatus = robotsTxt.getStatus(userAgent, urlItem);
		} else
			robotsTxtStatus = RobotsTxtStatus.DISABLED;
		urlItem.setRobotsTxtStatus(robotsTxtStatus);
		if (robotsTxtStatus == RobotsTxtStatus.DISABLED || robotsTxtStatus == RobotsTxtStatus.ALLOW)
			return true;
		if (robotsTxtStatus == RobotsTxtStatus.NO_ROBOTSTXT)
			return true;
		urlItem.setFetchStatus(FetchStatus.NOT_ALLOWED);
		return false;
	}

	/**
	 * Download the file and extract content informations
	 *
	 * @param httpDownloader
	 */
	public DownloadItem download(HttpDownloader httpDownloader) {
		synchronized (this) {
			InputStream is = null;
			DownloadItem downloadItem = null;
			try {
				URL url = urlItem.getURL();
				if (url == null)
					throw new MalformedURLException("Malformed URL: " + urlItem.getUrl());
				// URL normalisation
				URI uri = url.toURI();
				url = uri.toURL();

				credentialItem = credentialManager == null ? null : credentialManager.matchCredential(url);

				String externalFormUrl = url.toExternalForm();
				final CrawlCacheProvider.Item crawlCacheItem = crawlCacheManager.getItem(uri);
				downloadItem = crawlCacheItem.load();
				boolean fromCache = (downloadItem != null);

				if (!fromCache) {

					List<CookieItem> cookieList = cookieManager.getItems(externalFormUrl);
					List<HeaderItem> headerList = headerManager.getItems(externalFormUrl);
					downloadItem = httpDownloader.get(uri, credentialItem, headerList, cookieList);
				} else if (Logging.isDebug)
					Logging.debug("Crawl cache deliver: " + uri);

				urlItem.setContentDispositionFilename(downloadItem.getContentDispositionFilename());

				urlItem.setContentBaseType(downloadItem.getContentBaseType());

				urlItem.setContentTypeCharset(downloadItem.getContentTypeCharset());

				urlItem.setContentEncoding(downloadItem.getContentEncoding());

				urlItem.setContentLength(downloadItem.getContentLength());

				urlItem.setLastModifiedDate(downloadItem.getLastModified());

				urlItem.setFetchStatus(FetchStatus.FETCHED);

				urlItem.setHeaders(downloadItem.getHeaders());

				Integer code = downloadItem.getStatusCode();
				if (code == null)
					throw new IOException("Http status is null");

				urlItem.setResponseCode(code);
				redirectUrlLocation = downloadItem.getRedirectLocation();
				if (redirectUrlLocation != null)
					urlItem.setRedirectionUrl(redirectUrlLocation.toURL().toExternalForm());

				urlItem.setBacklinkCount(config.getUrlManager().countBackLinks(urlItem.getUrl()));

				if (code >= 200 && code < 300) {
					if (!fromCache)
						is = crawlCacheItem.store(downloadItem);
					else
						is = downloadItem.getContentInputStream();
					parseContent(is);
					if (parser != null)
						crawlCacheItem.store(parser.getParserResults());
				} else if (code == 301) {
					urlItem.setFetchStatus(FetchStatus.REDIR_PERM);
				} else if (code > 301 && code < 400) {
					urlItem.setFetchStatus(FetchStatus.REDIR_TEMP);
				} else if (code >= 400 && code < 500) {
					urlItem.setFetchStatus(FetchStatus.GONE);
				} else if (code >= 500 && code < 600) {
					urlItem.setFetchStatus(FetchStatus.HTTP_ERROR);
				}
			} catch (FileNotFoundException e) {
				Logging.info("FileNotFound: " + urlItem.getUrl());
				urlItem.setFetchStatus(FetchStatus.GONE);
				setError("FileNotFound: " + urlItem.getUrl());
			} catch (LimitException e) {
				Logging.warn(e.toString() + " (" + urlItem.getUrl() + ")");
				urlItem.setFetchStatus(FetchStatus.SIZE_EXCEED);
				setError(e.getMessage());
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				Logging.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (URISyntaxException | MalformedURLException e) {
				Logging.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			} catch (Exception e) {
				Logging.error(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			} finally {
				IOUtils.close(is);
			}
			return downloadItem;
		}
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public Parser getParser() {
		return parser;
	}

	public String getContentType() {
		if (urlItem == null)
			return null;
		return urlItem.getContentBaseType();
	}

	public StreamLimiter getStreamLimiter() {
		if (parser == null)
			return null;
		return parser.getStreamLimiter();
	}

	public String getError() {
		return error;
	}

	public UrlItem getUrlItem() {
		return urlItem;
	}

	public HostUrlList getHostUrlList() {
		return hostUrlList;
	}

	public CredentialItem getCredentialItem() {
		return credentialItem;
	}

	public IndexDocument getTargetIndexDocument(int documentPos)
			throws SearchLibException, IOException, URISyntaxException {
		if (targetIndexDocuments == null)
			getTargetIndexDocuments();
		if (targetIndexDocuments == null)
			return null;
		if (documentPos >= targetIndexDocuments.size())
			return null;
		return targetIndexDocuments.get(documentPos);
	}

	public List<IndexDocument> getTargetIndexDocuments() throws SearchLibException, IOException, URISyntaxException {
		synchronized (this) {
			if (targetIndexDocuments != null)
				return targetIndexDocuments;

			targetIndexDocuments = new ArrayList<>(0);

			if (parser == null)
				return targetIndexDocuments;

			List<ParserResultItem> results = parser.getParserResults();
			if (results == null)
				return targetIndexDocuments;

			for (ParserResultItem result : results) {
				IndexDocument targetIndexDocument = new IndexDocument(LanguageEnum.findByCode(urlItem.getLang()));

				IndexDocument urlIndexDocument = new IndexDocument();
				urlItem.populate(urlIndexDocument);
				urlFieldMap.mapIndexDocument(urlIndexDocument, targetIndexDocument);

				if (result != null)
					result.populate(targetIndexDocument);

				IndexPluginList indexPluginList = config.getWebCrawlMaster().getIndexPluginList();

				if (indexPluginList != null) {
					if (!indexPluginList.run((Client) config, getContentType(), getStreamLimiter(),
							targetIndexDocument)) {
						urlItem.setIndexStatus(IndexStatus.PLUGIN_REJECTED);
						urlItem.populate(urlIndexDocument);
						continue;
					}
				}

				targetIndexDocuments.add(targetIndexDocument);
			}
			return targetIndexDocuments;
		}
	}

	private void addDiscoverLink(String href, Origin origin, String parentUrl, int nextDepth, URL currentURL,
			UrlFilterItem[] urlFilterList, List<LinkItem> newUrlList) {
		if (href == null)
			return;
		try {
			URL url = currentURL != null ?
					LinkUtils.getLink(currentURL, href, urlFilterList, false) :
					LinkUtils.newEncodedURL(href);

			if (exclusionMatcher != null)
				if (exclusionMatcher.matchPattern(url, null))
					return;
			if (inclusionMatcher != null)
				if (!inclusionMatcher.matchPattern(url, null))
					return;
			newUrlList.add(new LinkItem(url.toExternalForm(), origin, parentUrl, nextDepth));
		} catch (MalformedURLException e) {
			Logging.warn(href + " " + e.getMessage(), e);
		} catch (URISyntaxException e) {
			Logging.warn(href + " " + e.getMessage(), e);
		}
	}

	private void addDiscoverLinks(Collection<String> linkSet, Origin origin, String parentUrl, int nextDepth,
			URL currentURL, UrlFilterItem[] urlFilterList, List<LinkItem> newUrlList)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
		if (linkSet == null)
			return;
		for (String link : linkSet)
			addDiscoverLink(link, origin, parentUrl, nextDepth, currentURL, urlFilterList, newUrlList);
	}

	public List<LinkItem> getDiscoverLinks()
			throws NoSuchAlgorithmException, IOException, SearchLibException, URISyntaxException {
		if (!linkDetectionEnabled)
			return null;
		synchronized (this) {
			if (discoverLinks != null)
				return discoverLinks;
			String parentUrl = urlItem.getUrl();
			int nextDepth = urlItem.getDepth() + 1;
			URL currentURL = urlItem.getURL();
			if (currentURL == null)
				return discoverLinks;
			discoverLinks = new ArrayList<>();
			if (redirectUrlLocation != null)
				addDiscoverLink(redirectUrlLocation.toString(), Origin.redirect, parentUrl, urlItem.getDepth(),
						currentURL, urlFilterList, discoverLinks);
			if (parser != null && urlItem.getFetchStatus() == FetchStatus.FETCHED)
				addDiscoverLinks(parser.getDetectedLinks(), Origin.content, parentUrl, nextDepth, currentURL,
						urlFilterList, discoverLinks);
			urlManager.removeExisting(discoverLinks);
			return discoverLinks;
		}
	}
}
