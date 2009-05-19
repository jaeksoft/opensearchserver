/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.ParseException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.web.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.web.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.process.CrawlStatistics;
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

	private UrlItem urlItem;
	private String userAgent;
	private ParserSelector parserSelector;
	private Config config;
	private Parser parser;
	private String error;
	private CrawlStatistics currentStats;
	private IndexDocument indexDocument;
	private List<String> discoverLinks;

	public Crawl(UrlItem urlItem, Config config, ParserSelector parserSelector,
			CrawlStatistics currentStats) throws SearchLibException {
		this.indexDocument = null;
		this.discoverLinks = null;
		this.currentStats = currentStats;
		this.urlItem = urlItem;
		this.urlItem.setWhenNow();
		this.userAgent = config.getPropertyManager().getUserAgent()
				.toLowerCase();
		this.parser = null;
		this.parserSelector = parserSelector;
		this.config = config;
		this.error = null;
	}

	private void parseContent(InputStream inputStream)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
		if (parserSelector == null)
			return;
		Parser parser = parserSelector.getParserFromMimeType(urlItem
				.getContentBaseType());
		if (parser == null) {
			urlItem.setParserStatus(ParserStatus.NOPARSER);
			return;
		}
		parser.parseContent(inputStream);
		this.parser = parser;
	}

	public boolean checkRobotTxtAllow(HttpDownloader httpDownloader)
			throws MalformedURLException, SearchLibException {
		RobotsTxt robotsTxt = config.getRobotsTxtCache().getRobotsTxt(
				httpDownloader, config, urlItem.getURL(), false);
		RobotsTxtStatus robotsTxtStatus = robotsTxt.status(userAgent);
		urlItem.setRobotsTxtStatus(robotsTxtStatus);
		if (robotsTxtStatus != RobotsTxtStatus.ALLOW
				&& robotsTxtStatus != RobotsTxtStatus.NO_ROBOTSTXT) {
			urlItem.setFetchStatus(FetchStatus.NOT_ALLOWED);
			return false;
		}
		return true;
	}

	/**
	 * T�l�charge le fichier et extrait les informations
	 * 
	 * @param userAgent
	 */
	public void download(HttpDownloader httpDownloader) {
		synchronized (this) {
			GetMethod getMethod = null;
			InputStream is = null;
			try {
				getMethod = httpDownloader.get(urlItem.getCheckedURI()
						.toASCIIString(), userAgent);
				is = getMethod.getResponseBodyAsStream();
				Header header = getMethod.getResponseHeader("Content-Type");
				if (header != null)
					urlItem.setContentType(header.getValue());
				urlItem.setContentEncoding(getMethod.getResponseCharSet());
				urlItem.setContentLength((int) getMethod
						.getResponseContentLength());
				urlItem.setFetchStatus(FetchStatus.FETCHED);
				int code = getMethod.getStatusCode();
				urlItem.setResponseCode(code);
				if (code >= 200 && code < 300) {
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
			} catch (ParseException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setParserStatus(ParserStatus.PARSER_ERROR);
				setError(e.getMessage());
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.URL_ERROR);
				setError(e.getMessage());
			} catch (HttpException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				urlItem.setFetchStatus(FetchStatus.HTTP_ERROR);
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
			} finally {
				httpDownloader.release();
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

	public IndexDocument getIndexDocument() throws SearchLibException,
			MalformedURLException {
		synchronized (this) {
			if (indexDocument != null)
				return indexDocument;
			indexDocument = null;
			if (parser != null)
				indexDocument = parser.getDocument();
			if (indexDocument == null)
				indexDocument = new IndexDocument();
			urlItem.populate(indexDocument);
			IndexPluginList indexPluginList = config.getWebCrawlMaster()
					.getIndexPluginList();
			if (indexPluginList != null && !indexPluginList.run(indexDocument)) {
				urlItem.setIndexStatus(IndexStatus.INDEX_REJECTED);
				indexDocument = new IndexDocument();
				urlItem.populate(indexDocument);
			}
			if (currentStats != null)
				currentStats.incPendingUpdatedCount();
			return indexDocument;
		}
	}

	final private static void discoverLinks(UrlManager urlManager,
			PatternManager patternManager, FieldContent urlFieldContent,
			List<String> newUrlList) throws NoSuchAlgorithmException,
			IOException, SearchLibException {
		if (urlFieldContent == null)
			return;
		List<String> links = urlFieldContent.getValues();
		if (links == null)
			return;
		for (String link : links) {
			try {
				URL url = new URL(link);
				String sUrl = url.toExternalForm();
				if (patternManager.findPattern(url) != null)
					if (!urlManager.exists(sUrl))
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
			if (parser == null || !urlItem.isStatusFull())
				return null;
			discoverLinks = new ArrayList<String>();
			UrlManager urlManager = config.getUrlManager();
			PatternManager patternUrlManager = config.getPatternManager();
			discoverLinks(urlManager, patternUrlManager, parser
					.getFieldContent(ParserFieldEnum.internal_link),
					discoverLinks);
			discoverLinks(urlManager, patternUrlManager, parser
					.getFieldContent(ParserFieldEnum.external_link),
					discoverLinks);
			if (currentStats != null)
				currentStats.addPendingNewUrlCount(discoverLinks.size());
			return discoverLinks;
		}
	}

}
