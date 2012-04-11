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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem;
import com.jaeksoft.searchlib.crawler.web.database.LinkItem.Origin;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlItemFieldEnum;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginList;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class Crawl {

	private IndexDocument targetIndexDocument;
	private HostUrlList hostUrlList;
	private UrlItem urlItem;
	private CredentialManager credentialManager;
	private CredentialItem credentialItem;
	private String userAgent;
	private ParserSelector parserSelector;
	private Config config;
	private Parser parser;
	private String error;
	private List<LinkItem> discoverLinks;
	private FieldMap urlFieldMap;
	private URI redirectUrlLocation;
	private boolean inclusionEnabled;
	private boolean exclusionEnabled;
	private boolean robotsTxtEnabled;
	private final UrlItemFieldEnum urlItemFieldEnum;

	public Crawl(HostUrlList hostUrlList, UrlItem urlItem, Config config,
			ParserSelector parserSelector) throws SearchLibException {
		this.credentialManager = config.getWebCredentialManager();
		this.credentialItem = null;
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		this.hostUrlList = hostUrlList;
		this.targetIndexDocument = null;
		this.urlFieldMap = config.getWebCrawlerFieldMap();
		this.discoverLinks = null;
		this.urlItem = urlItem;
		this.urlItem.setWhenNow();
		this.userAgent = propertyManager.getUserAgent().getValue();
		this.parser = null;
		this.parserSelector = parserSelector;
		this.config = config;
		this.error = null;
		this.redirectUrlLocation = null;
		this.exclusionEnabled = propertyManager.getExclusionEnabled()
				.getValue();
		this.inclusionEnabled = propertyManager.getInclusionEnabled()
				.getValue();
		this.robotsTxtEnabled = propertyManager.getRobotsTxtEnabled()
				.getValue();
		this.urlItemFieldEnum = config.getUrlManager().getUrlItemFieldEnum();

	}

	public Crawl(WebCrawlThread crawlThread) throws SearchLibException {
		this(crawlThread.getHostUrlList(), crawlThread.getCurrentUrlItem(),
				crawlThread.getConfig(), crawlThread.getConfig()
						.getParserSelector());
	}

	protected void parseContent(InputStream inputStream)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException, SearchLibException,
			NoSuchAlgorithmException {
		if (parserSelector == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}
		String fileName = urlItem.getContentDispositionFilename();
		if (fileName == null)
			fileName = urlItem.getURL().getFile();
		Parser parser = parserSelector.getParser(fileName,
				urlItem.getContentBaseType());
		if (parser == null)
			parser = parserSelector.getWebCrawlerDefaultParser();
		if (parser == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}
		IndexDocument sourceDocument = new IndexDocument();
		urlItem.populate(sourceDocument, urlItemFieldEnum);
		parser.setSourceDocument(sourceDocument);
		Date parserStartDate = new Date();
		// TODO Which language for OCR ?
		parser.parseContent(inputStream, null);

		urlItem.clearInLinks();
		urlItem.addInLinks(parser
				.getFieldContent(ParserFieldEnum.internal_link));
		urlItem.addInLinks(parser
				.getFieldContent(ParserFieldEnum.internal_link_nofollow));
		urlItem.clearOutLinks();
		urlItem.addOutLinks(parser
				.getFieldContent(ParserFieldEnum.external_link));
		urlItem.addOutLinks(parser
				.getFieldContent(ParserFieldEnum.external_link_nofollow));
		urlItem.setLang(parser.getFieldValue(ParserFieldEnum.lang, 0));
		urlItem.setLangMethod(parser.getFieldValue(ParserFieldEnum.lang_method,
				0));
		urlItem.setContentTypeCharset(parser.getFieldValue(
				ParserFieldEnum.charset, 0));
		urlItem.setParserStatus(ParserStatus.PARSED);
		String oldMd5size = urlItem.getMd5size();
		String newMd5size = parser.getMd5size();
		urlItem.setMd5size(newMd5size);
		Date oldLastModifiedTime = urlItem.getLastModifiedDate();
		Date newLastModifiedTime = null;
		if (oldLastModifiedTime == null)
			newLastModifiedTime = parserStartDate;
		else {
			if (oldMd5size != null && newMd5size != null)
				if (!oldMd5size.equals(newMd5size))
					newLastModifiedTime = parserStartDate;
		}
		if (newLastModifiedTime != null)
			urlItem.setLastModifiedDate(newLastModifiedTime);

		FieldContent fieldContent = parser
				.getFieldContent(ParserFieldEnum.meta_robots);
		if (fieldContent != null) {
			List<FieldValueItem> fieldValues = fieldContent.getValues();
			if (fieldValues != null) {
				for (FieldValueItem item : parser.getFieldContent(
						ParserFieldEnum.meta_robots).getValues())
					if ("noindex".equalsIgnoreCase(item.getValue())) {
						urlItem.setIndexStatus(IndexStatus.META_NOINDEX);
						break;
					}
			}
		}

		this.parser = parser;
	}

	public boolean checkRobotTxtAllow(HttpDownloader httpDownloader)
			throws MalformedURLException, SearchLibException {
		RobotsTxtStatus robotsTxtStatus;
		if (robotsTxtEnabled) {
			RobotsTxt robotsTxt = config.getRobotsTxtCache().getRobotsTxt(
					httpDownloader, config, urlItem.getURL(), false);
			robotsTxtStatus = robotsTxt.getStatus(userAgent, urlItem);
		} else
			robotsTxtStatus = RobotsTxtStatus.DISABLED;
		urlItem.setRobotsTxtStatus(robotsTxtStatus);
		if (robotsTxtStatus == RobotsTxtStatus.DISABLED
				|| robotsTxtStatus == RobotsTxtStatus.ALLOW)
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
	public void download(HttpDownloader httpDownloader) {
		synchronized (this) {
			InputStream is = null;
			try {
				URI uri = urlItem.getCheckedURI();

				credentialItem = credentialManager == null ? null
						: credentialManager.matchCredential(uri.toURL());

				DownloadItem downloadItem = ClientCatalog
						.getCrawlCacheManager().loadCache(uri);

				boolean fromCache = (downloadItem != null);

				if (!fromCache)
					downloadItem = httpDownloader.get(uri, credentialItem);

				urlItem.setContentDispositionFilename(downloadItem
						.getContentDispositionFilename());

				urlItem.setContentBaseType(downloadItem.getContentBaseType());

				urlItem.setContentTypeCharset(downloadItem
						.getContentTypeCharset());

				urlItem.setContentEncoding(downloadItem.getContentEncoding());

				urlItem.setContentLength(downloadItem.getContentLength());

				urlItem.setFetchStatus(FetchStatus.FETCHED);

				Integer code = downloadItem.getStatusCode();
				if (code == null)
					throw new IOException("Http status is null");

				urlItem.setResponseCode(code);
				redirectUrlLocation = downloadItem.getRedirectLocation();

				if (code >= 200 && code < 300) {
					if (!fromCache)
						is = ClientCatalog.getCrawlCacheManager().storeCache(
								downloadItem);
					else
						is = downloadItem.getContentInputStream();
					parseContent(is);
				} else if ("301".equals(code)) {
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
			} catch (InstantiationException e) {
				Logging.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IllegalAccessException e) {
				Logging.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (ClassNotFoundException e) {
				Logging.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (URISyntaxException e) {
				Logging.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (MalformedURLException e) {
				Logging.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				Logging.error(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			} catch (Exception e) {
				Logging.error(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			}
			if (is != null)
				IOUtils.closeQuietly(is);
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

	public IndexDocument getTargetIndexDocument() throws SearchLibException,
			IOException {
		synchronized (this) {
			if (targetIndexDocument != null)
				return targetIndexDocument;

			targetIndexDocument = new IndexDocument(
					LanguageEnum.findByCode(urlItem.getLang()));

			IndexDocument urlIndexDocument = new IndexDocument();
			urlItem.populate(urlIndexDocument, urlItemFieldEnum);
			urlFieldMap.mapIndexDocument(urlIndexDocument, targetIndexDocument);

			if (parser != null)
				parser.populate(targetIndexDocument);

			IndexPluginList indexPluginList = config.getWebCrawlMaster()
					.getIndexPluginList();

			if (indexPluginList != null) {
				if (!indexPluginList.run((Client) config, getContentType(),
						getStreamLimiter(), targetIndexDocument)) {
					urlItem.setIndexStatus(IndexStatus.PLUGIN_REJECTED);
					urlItem.populate(urlIndexDocument, urlItemFieldEnum);
				}
			}

			return targetIndexDocument;
		}
	}

	final private static void discoverLinks(UrlManager urlManager,
			PatternManager inclusionManager, PatternManager exclusionManager,
			FieldContent urlFieldContent, Origin origin, String parentUrl,
			List<LinkItem> newUrlList) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		if (urlFieldContent == null)
			return;
		List<FieldValueItem> links = urlFieldContent.getValues();
		if (links == null)
			return;
		for (FieldValueItem linkItem : links) {
			String link = linkItem.getValue();
			try {
				URL url = new URL(link);
				if (exclusionManager != null)
					if (exclusionManager.matchPattern(url))
						continue;
				if (inclusionManager != null)
					if (!inclusionManager.matchPattern(url))
						continue;
				newUrlList.add(new LinkItem(link, origin, parentUrl));
			} catch (MalformedURLException e) {
				Logging.warn(link + " " + e.getMessage(), e);
			}
		}
		urlManager.removeExisting(newUrlList);
	}

	public List<LinkItem> getDiscoverLinks() throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		synchronized (this) {
			if (discoverLinks != null)
				return discoverLinks;
			String parentUrl = urlItem.getUrl();
			discoverLinks = new ArrayList<LinkItem>();
			if (redirectUrlLocation != null) {
				discoverLinks.add(new LinkItem(redirectUrlLocation.toString(),
						Origin.redirect, parentUrl));
				return discoverLinks;
			}
			if (parser == null || !urlItem.isStatusFull())
				return discoverLinks;
			UrlManager urlManager = config.getUrlManager();
			PatternManager inclusionManager = inclusionEnabled ? config
					.getInclusionPatternManager() : null;
			PatternManager exclusionManager = exclusionEnabled ? config
					.getExclusionPatternManager() : null;
			discoverLinks(urlManager, inclusionManager, exclusionManager,
					parser.getFieldContent(ParserFieldEnum.internal_link),
					Origin.content, parentUrl, discoverLinks);
			discoverLinks(urlManager, inclusionManager, exclusionManager,
					parser.getFieldContent(ParserFieldEnum.external_link),
					Origin.content, parentUrl, discoverLinks);
			discoverLinks(urlManager, inclusionManager, exclusionManager,
					parser.getFieldContent(ParserFieldEnum.frameset_link),
					Origin.frameset, parentUrl, discoverLinks);
			return discoverLinks;
		}
	}
}
