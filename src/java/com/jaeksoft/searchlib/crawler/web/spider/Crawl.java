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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.LimitException;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.plugin.IndexPluginList;

public class Crawl {

	final private static Logger logger = Logger.getLogger(Crawl.class
			.getCanonicalName());

	private IndexDocument targetIndexDocument;
	private UrlItem urlItem;
	private String userAgent;
	private ParserSelector parserSelector;
	private Config config;
	private Parser parser;
	private String error;
	private List<String> discoverLinks;
	private FieldMap urlFieldMap;
	private URI redirectUrlLocation;

	public Crawl(UrlItem urlItem, Config config, ParserSelector parserSelector)
			throws SearchLibException {
		this.targetIndexDocument = null;
		this.urlFieldMap = config.getWebCrawlerFieldMap();
		this.discoverLinks = null;
		this.urlItem = urlItem;
		this.urlItem.setWhenNow();
		this.userAgent = config.getWebPropertyManager().getUserAgent()
				.getValue().toLowerCase();
		this.parser = null;
		this.parserSelector = parserSelector;
		this.config = config;
		this.error = null;
		this.redirectUrlLocation = null;
	}

	private void parseContent(InputStream inputStream)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException, SearchLibException {
		if (parserSelector == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}
		Parser parser = parserSelector.getParserFromMimeType(urlItem
				.getContentBaseType());
		if (parser == null)
			parser = parserSelector.getWebCrawlerDefaultParser();
		if (parser == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}

		IndexDocument sourceDocument = new IndexDocument();
		urlItem.populate(sourceDocument);
		parser.setSourceDocument(sourceDocument);
		parser.parseContent(inputStream);
		urlItem.setLang(parser.getFieldValue(ParserFieldEnum.lang, 0));
		urlItem.setLangMethod(parser.getFieldValue(ParserFieldEnum.lang_method,
				0));
		urlItem.setContentTypeCharset(parser.getFieldValue(
				ParserFieldEnum.charset, 0));
		urlItem.setParserStatus(ParserStatus.PARSED);
		this.parser = parser;
	}

	public boolean checkRobotTxtAllow(HttpDownloader httpDownloader)
			throws MalformedURLException, SearchLibException {
		RobotsTxt robotsTxt = config.getRobotsTxtCache().getRobotsTxt(
				httpDownloader, config, urlItem.getURL(), false);
		RobotsTxtStatus robotsTxtStatus = robotsTxt.getStatus(userAgent,
				urlItem);
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
				httpDownloader.get(urlItem.getCheckedURI().toASCIIString());

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
				logger.info("FileNotFound: " + urlItem.getUrl());
				urlItem.setFetchStatus(FetchStatus.GONE);
				setError("FileNotFound: " + urlItem.getUrl());
			} catch (LimitException e) {
				logger.warning(e.toString() + " (" + urlItem.getUrl() + ")");
				urlItem.setFetchStatus(FetchStatus.SIZE_EXCEED);
				setError(e.getMessage());
			} catch (InstantiationException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (URISyntaxException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.ERROR);
				setError(e.getMessage());
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				e.printStackTrace();
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

	public String getError() {
		return error;
	}

	public UrlItem getUrlItem() {
		return urlItem;
	}

	public IndexDocument getTargetIndexDocument() throws SearchLibException,
			MalformedURLException {
		synchronized (this) {
			if (targetIndexDocument != null)
				return targetIndexDocument;

			IndexDocument targetIndexDocument = new IndexDocument(
					LanguageEnum.findByCode(urlItem.getLang()));

			IndexDocument urlIndexDocument = new IndexDocument();
			urlItem.populate(urlIndexDocument);
			urlFieldMap.mapIndexDocument(urlIndexDocument, targetIndexDocument);

			if (parser != null)
				parser.populate(targetIndexDocument);

			IndexPluginList indexPluginList = config.getWebCrawlMaster()
					.getIndexPluginList();
			if (indexPluginList != null
					&& !indexPluginList.run(targetIndexDocument)) {
				urlItem.setIndexStatus(IndexStatus.PLUGIN_REJECTED);
				urlItem.populate(urlIndexDocument);
			}

			return targetIndexDocument;
		}
	}

	final private static void discoverLinks(UrlManager urlManager,
			PatternManager inclusionManager, PatternManager exclusionManager,
			FieldContent urlFieldContent, List<String> newUrlList)
			throws NoSuchAlgorithmException, IOException, SearchLibException {
		if (urlFieldContent == null)
			return;
		List<String> links = urlFieldContent.getValues();
		if (links == null)
			return;
		for (String link : links) {
			try {
				URL url = new URL(link);
				String sUrl = url.toExternalForm();
				if (exclusionManager.matchPattern(url) != null)
					continue;
				if (inclusionManager.matchPattern(url) == null)
					continue;
				if (urlManager.exists(sUrl))
					continue;
				newUrlList.add(link);
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, link + " " + e.getMessage(), e);
			}
		}
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
			UrlManager urlManager = config.getUrlManager();
			PatternManager inclusionManager = config
					.getInclusionPatternManager();
			PatternManager exclusionManager = config
					.getExclusionPatternManager();
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
