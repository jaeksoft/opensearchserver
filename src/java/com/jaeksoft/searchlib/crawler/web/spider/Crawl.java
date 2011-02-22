/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
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
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManagerAbstract;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.LimitException;
import com.jaeksoft.searchlib.parser.LimitInputStream;
import com.jaeksoft.searchlib.parser.LimitReader;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginList;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class Crawl {

	private IndexDocument targetIndexDocument;
	private HostUrlList hostUrlList;
	private UrlItem urlItem;
	private CredentialManager credentialManager;
	private String userAgent;
	private ParserSelector parserSelector;
	private Config config;
	private Parser parser;
	private String error;
	private List<String> discoverLinks;
	private FieldMap urlFieldMap;
	private URI redirectUrlLocation;
	private boolean inclusionEnabled;
	private boolean exclusionEnabled;
	private boolean robotsTxtEnabled;

	public Crawl(HostUrlList hostUrlList, UrlItem urlItem, Config config,
			ParserSelector parserSelector, CredentialManager credentialManager)
			throws SearchLibException {
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		this.hostUrlList = hostUrlList;
		this.targetIndexDocument = null;
		this.urlFieldMap = config.getWebCrawlerFieldMap();
		this.discoverLinks = null;
		this.urlItem = urlItem;
		this.urlItem.setWhenNow();
		this.credentialManager = credentialManager;
		this.userAgent = propertyManager.getUserAgent().getValue()
				.toLowerCase();
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
	}

	private void parseContent(InputStream inputStream)
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
		urlItem.populate(sourceDocument);
		parser.setSourceDocument(sourceDocument);
		Date parserStartDate = new Date();
		parser.parseContent(inputStream);
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
		if (robotsTxtStatus != RobotsTxtStatus.ALLOW
				&& robotsTxtStatus != RobotsTxtStatus.NO_ROBOTSTXT) {
			urlItem.setFetchStatus(FetchStatus.NOT_ALLOWED);
			return false;
		}
		return true;
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

				CredentialItem credentialItem = credentialManager == null ? null
						: credentialManager.matchCredential(uri.toURL());
				httpDownloader.get(uri, credentialItem);

				String contentDispositionFilename = httpDownloader
						.getContentDispositionFilename();
				urlItem.setContentDispositionFilename(contentDispositionFilename);

				String contentBaseType = httpDownloader.getContentBaseType();
				urlItem.setContentBaseType(contentBaseType);

				String contentTypeCharset = httpDownloader
						.getContentTypeCharset();
				urlItem.setContentTypeCharset(contentTypeCharset);

				String encoding = httpDownloader.getContentEncoding();
				urlItem.setContentEncoding(encoding);

				Long contentLength = httpDownloader.getContentLength();
				urlItem.setContentLength(contentLength);

				urlItem.setFetchStatus(FetchStatus.FETCHED);

				Integer code = httpDownloader.getStatusCode();
				if (code == null)
					throw new IOException("Http status is null");

				urlItem.setResponseCode(code);
				redirectUrlLocation = httpDownloader.getRedirectLocation();

				if (code >= 200 && code < 300) {
					is = httpDownloader.getContent();
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
				Logging.logger.info("FileNotFound: " + urlItem.getUrl());
				urlItem.setFetchStatus(FetchStatus.GONE);
				setError("FileNotFound: " + urlItem.getUrl());
			} catch (LimitException e) {
				Logging.logger.warn(e.toString() + " (" + urlItem.getUrl()
						+ ")");
				urlItem.setFetchStatus(FetchStatus.SIZE_EXCEED);
				setError(e.getMessage());
			} catch (InstantiationException e) {
				Logging.logger.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IllegalAccessException e) {
				Logging.logger.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (ClassNotFoundException e) {
				Logging.logger.error(e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (URISyntaxException e) {
				Logging.logger.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (MalformedURLException e) {
				Logging.logger.warn(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				Logging.logger.error(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			} catch (Exception e) {
				Logging.logger.error(e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				Logging.logger.warn(e.getMessage(), e);
			}
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

	public LimitInputStream getInputStream() {
		if (parser == null)
			return null;
		return parser.getLimitInputStream();
	}

	public LimitReader getReader() {
		if (parser == null)
			return null;
		return parser.getLimitReader();
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

	public IndexDocument getTargetIndexDocument() throws SearchLibException,
			MalformedURLException {
		synchronized (this) {
			if (targetIndexDocument != null)
				return targetIndexDocument;

			targetIndexDocument = new IndexDocument(
					LanguageEnum.findByCode(urlItem.getLang()));

			IndexDocument urlIndexDocument = new IndexDocument();
			urlItem.populate(urlIndexDocument);
			urlFieldMap.mapIndexDocument(urlIndexDocument, targetIndexDocument);

			if (parser != null)
				parser.populate(targetIndexDocument);

			IndexPluginList indexPluginList = config.getWebCrawlMaster()
					.getIndexPluginList();

			if (indexPluginList != null) {
				if (!indexPluginList.run((Client) config, getContentType(),
						getInputStream(), getReader(), targetIndexDocument)) {
					urlItem.setIndexStatus(IndexStatus.PLUGIN_REJECTED);
					urlItem.populate(urlIndexDocument);
				}
			}

			return targetIndexDocument;
		}
	}

	final private static void discoverLinks(UrlManagerAbstract urlManager,
			PatternManager inclusionManager, PatternManager exclusionManager,
			FieldContent urlFieldContent, List<String> newUrlList)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
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
				newUrlList.add(link);
			} catch (MalformedURLException e) {
				Logging.logger.warn(link + " " + e.getMessage(), e);
			}
		}
		urlManager.removeExisting(newUrlList);
	}

	public List<String> getDiscoverLinks() throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		synchronized (this) {
			if (discoverLinks != null)
				return discoverLinks;
			discoverLinks = new ArrayList<String>();
			if (redirectUrlLocation != null) {
				discoverLinks.add(redirectUrlLocation.toString());
				return discoverLinks;
			}
			if (parser == null || !urlItem.isStatusFull())
				return discoverLinks;
			UrlManagerAbstract urlManager = config.getUrlManager();
			PatternManager inclusionManager = inclusionEnabled ? config
					.getInclusionPatternManager() : null;
			PatternManager exclusionManager = exclusionEnabled ? config
					.getExclusionPatternManager() : null;
			discoverLinks(urlManager, inclusionManager, exclusionManager,
					parser.getFieldContent(ParserFieldEnum.internal_link),
					discoverLinks);
			discoverLinks(urlManager, inclusionManager, exclusionManager,
					parser.getFieldContent(ParserFieldEnum.external_link),
					discoverLinks);
			return discoverLinks;
		}
	}
}
