/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.spider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.robotstxt.RobotsTxt;
import com.jaeksoft.searchlib.crawler.robotstxt.RobotsTxtCache;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Crawl implements XmlInfo {

	final private static Logger logger = Logger.getLogger(Crawl.class);

	public enum Status {
		CRAWLED, NOPARSER, REDIR_PERM, REDIR_TEMP, HTTP_GONE, HTTP_ERROR, ERROR;
	}

	public class CrawlException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5356666312363522180L;

		public CrawlException(String msg) {
			super(msg);
		}
	}

	private URL url;
	private String userAgent;
	private int httpResponseCode;
	private ContentType contentType;
	private String contentEncoding;
	private int contentLength;
	private ParserSelector parserSelector;
	private Parser parser;
	private IndexDocument document;
	private Status status;
	private String error;

	private void start(URL url, String userAgent, ParserSelector parserSelector) {
		this.url = url;
		this.userAgent = userAgent.toLowerCase();
		this.httpResponseCode = 0;
		this.contentType = null;
		this.contentEncoding = null;
		this.contentLength = 0;
		this.parser = null;
		this.parserSelector = parserSelector;
		this.document = null;
		this.status = null;
		this.error = null;
		download();
	}

	public Crawl(Config config, String userAgent, URL url)
			throws CrawlException, MalformedURLException {
		RobotsTxtCache robotsTxtCache = config.getRobotsTxtCache();
		RobotsTxt robotsTxt = robotsTxtCache
				.getRobotsTxt(userAgent, url, false);
		if (!robotsTxt.isAllowed(url, userAgent))
			throw new CrawlException("Refused by robots.txt - "
					+ robotsTxt.getCrawl().getUrl());
		start(url, userAgent, config.getParserSelector());
	}

	public Crawl(URL url, String userAgent, ParserSelector parserSelector) {
		start(url, userAgent, parserSelector);
	}

	private void parseContent(InputStream inputStream) {
		if (parserSelector == null)
			return;
		Parser parser;
		try {
			parser = parserSelector.getParser(contentType.getBaseType(), url);
			if (parser == null) {
				status = Status.NOPARSER;
				return;
			}
			parser.parseContent(this, inputStream);
			status = Status.CRAWLED;
			this.parser = parser;
		} catch (InstantiationException e) {
			logger.info(e + " (" + url + ")");
			setStatusError(Status.ERROR, e.getMessage());
		} catch (IllegalAccessException e) {
			logger.info(e + " (" + url + ")");
			setStatusError(Status.ERROR, e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.info(e + " (" + url + ")");
			setStatusError(Status.ERROR, e.getMessage());
		} catch (IOException e) {
			logger.info(e + " (" + url + ")");
			setStatusError(Status.ERROR, e.getMessage());
		}
	}

	/**
	 * T�l�charge le fichier et extrait les informations
	 * 
	 * @param userAgent
	 */
	private void download() {
		synchronized (this) {
			HttpURLConnection huc = null;
			InputStream is = null;
			try {
				huc = (HttpURLConnection) this.url.openConnection();
				if (userAgent != null)
					huc.addRequestProperty("User-agent", userAgent);
				huc.setConnectTimeout(60000);
				huc.setReadTimeout(60000);
				is = huc.getInputStream();
				httpResponseCode = huc.getResponseCode();
				contentType = new ContentType(huc.getContentType());
				contentEncoding = huc.getContentEncoding();
				contentLength = huc.getContentLength();
				String code = Integer.toString(httpResponseCode);
				if (httpResponseCode == 200)
					if (code.startsWith("2")) {
						parseContent(is);
					} else if ("301".equals(code)) {
						status = Status.REDIR_PERM;
					} else if (code.startsWith("3")) {
						status = Status.REDIR_TEMP;
					} else if (code.startsWith("4")) {
						status = Status.HTTP_GONE;
					} else if (code.startsWith("5")) {
						status = Status.HTTP_ERROR;
					}
			} catch (FileNotFoundException e) {
				logger.info("FileNotFound: " + url);
				httpResponseCode = 404;
				setStatusError(Status.HTTP_GONE, "FileNotFound: " + url);
			} catch (IOException e) {
				logger.warn(e.toString() + " (" + url + ")");
				setStatusError(Status.ERROR, e.getMessage());
			} catch (ParseException e) {
				logger.warn(e.toString() + " (" + url + ")");
				setStatusError(Status.ERROR, e.getMessage());
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
			if (huc != null)
				huc.disconnect();
		}
	}

	private void setStatusError(Status status, String error) {
		this.status = status;
		this.error = error;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public URL getUrl() {
		return url;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public int getContentLength() {
		return contentLength;
	}

	public Parser getParser() {
		return parser;
	}

	public String getError() {
		return error;
	}

	public Status getStatus() {
		return status;
	}

	public IndexDocument getDocument() {
		if (document != null)
			return document;
		if (parser == null)
			return null;
		document = parser.getDocument();
		String sUrl = url.toExternalForm();
		document.add("url", sUrl);
		document.add("url_key", sUrl);
		return document;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.print("<crawl url=\""
				+ StringEscapeUtils.escapeXml(url.toExternalForm())
				+ "\" httpResponseCode=\"" + httpResponseCode
				+ "\" contentLength=\"" + contentLength + "\" contentType=\""
				+ contentType + "\" contentEncoding=\"" + contentEncoding
				+ "\">");
		if (parser != null)
			parser.xmlInfo(writer, classDetail);
		if (document != null)
			document.xmlInfo(writer, classDetail);
		writer.println("</crawl>");

	}

}
