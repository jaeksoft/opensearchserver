/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.ContentType;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

public class UrlItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043010587042224473L;

	final private static Logger logger = Logger.getLogger(UrlItem.class
			.getCanonicalName());

	private String url;
	private String contentBaseType;
	private String contentTypeCharset;
	private Integer contentLength;
	private String contentEncoding;
	private String lang;
	private String langMethod;
	private URL cachedUrl;
	private URI checkedUri;
	private String host;
	private Date when;
	private String metaDescription;
	private String metaKeywords;
	private RobotsTxtStatus robotsTxtStatus;
	private FetchStatus fetchStatus;
	private Integer responseCode;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;
	private int count;

	public UrlItem() {
		url = null;
		cachedUrl = null;
		checkedUri = null;
		contentBaseType = null;
		contentTypeCharset = null;
		contentLength = null;
		contentEncoding = null;
		lang = null;
		langMethod = null;
		host = null;
		when = new Date();
		robotsTxtStatus = RobotsTxtStatus.UNKNOWN;
		fetchStatus = FetchStatus.UN_FETCHED;
		responseCode = null;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
		count = 0;
	}

	public UrlItem(ResultDocument doc) {
		this();
		setUrl(doc.getValue("url", 0));
		setHost(doc.getValue("host", 0));
		setContentBaseType(doc.getValue("contentBaseType", 0));
		setContentTypeCharset(doc.getValue("contentTypeCharset", 0));
		setContentLength(doc.getValue("contentLength", 0));
		setContentEncoding(doc.getValue("contentEncoding", 0));
		setMetaDescription(doc.getValue("metaDescription", 0));
		setMetaKeywords(doc.getValue("metaKeywords", 0));
		setLang(doc.getValue("lang", 0));
		setLangMethod(doc.getValue("langMethod", 0));
		setWhen(doc.getValue("when", 0));
		setRobotsTxtStatusInt(doc.getValue("robotsTxtStatus", 0));
		setFetchStatusInt(doc.getValue("fetchStatus", 0));
		setResponseCode(doc.getValue("responseCode", 0));
		setParserStatusInt(doc.getValue("parserStatus", 0));
		setIndexStatusInt(doc.getValue("indexStatus", 0));
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void checkHost() throws MalformedURLException {
		setHost(getURL().getHost());
	}

	public FetchStatus getFetchStatus() {
		if (fetchStatus == null)
			return FetchStatus.UN_FETCHED;
		return fetchStatus;
	}

	public void setParserStatus(ParserStatus status) {
		this.parserStatus = status;
	}

	public void setParserStatusInt(int v) {
		this.parserStatus = ParserStatus.find(v);
	}

	private void setParserStatusInt(String v) {
		if (v != null)
			setParserStatusInt(Integer.parseInt(v));
	}

	public void setContentType(String v)
			throws javax.mail.internet.ParseException {
		ContentType contentType = new ContentType(v);
		setContentBaseType(contentType.getBaseType());
		setContentTypeCharset(contentType.getParameter("charset"));
	}

	public String getContentTypeCharset() {
		return contentTypeCharset;
	}

	public void setContentTypeCharset(String v) {
		contentTypeCharset = v;
	}

	public String getContentBaseType() {
		return contentBaseType;
	}

	public void setContentBaseType(String v) {
		contentBaseType = v;
	}

	public void setContentEncoding(String v) {
		contentEncoding = v;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	private void setContentLength(String v) {
		if (v == null)
			return;
		if (v.length() == 0)
			return;
		try {
			contentLength = getContentLengthFormat().parse(v).intValue();
		} catch (ParseException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public void setContentLength(int v) {
		contentLength = v;
	}

	public Integer getContentLength() {
		return contentLength;
	}

	public void setMetaDescription(String v) {
		metaDescription = v;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaKeywords(String v) {
		metaKeywords = v;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public ParserStatus getParserStatus() {
		if (parserStatus == null)
			return ParserStatus.NOT_PARSED;
		return parserStatus;
	}

	public void setIndexStatus(IndexStatus status) {
		this.indexStatus = status;
	}

	public void setIndexStatusInt(int v) {
		this.indexStatus = IndexStatus.find(v);
	}

	private void setIndexStatusInt(String v) {
		if (v != null)
			setIndexStatusInt(Integer.parseInt(v));
	}

	public IndexStatus getIndexStatus() {
		if (indexStatus == null)
			return IndexStatus.NOT_INDEXED;
		return indexStatus;
	}

	public RobotsTxtStatus getRobotsTxtStatus() {
		if (robotsTxtStatus == null)
			return RobotsTxtStatus.UNKNOWN;
		return robotsTxtStatus;
	}

	public void setRobotsTxtStatus(RobotsTxtStatus status) {
		this.robotsTxtStatus = status;
	}

	public void setRobotsTxtStatusInt(int v) {
		this.robotsTxtStatus = RobotsTxtStatus.find(v);
	}

	private void setRobotsTxtStatusInt(String v) {
		if (v != null)
			setRobotsTxtStatusInt(Integer.parseInt(v));
	}

	public void setFetchStatus(FetchStatus status) {
		this.fetchStatus = status;
	}

	public void setFetchStatusInt(int v) {
		this.fetchStatus = FetchStatus.find(v);
	}

	private void setFetchStatusInt(String v) {
		if (v != null)
			setFetchStatusInt(Integer.parseInt(v));
	}

	private void setResponseCode(String v) {
		if (v != null)
			responseCode = new Integer(v);
	}

	public void setResponseCode(Integer v) {
		responseCode = v;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public URL getURL() throws MalformedURLException {
		synchronized (this) {
			if (cachedUrl == null)
				cachedUrl = new URL(url);
			return cachedUrl;
		}
	}

	public URI getCheckedURI() throws MalformedURLException, URISyntaxException {
		synchronized (this) {
			if (checkedUri != null)
				return checkedUri;
			URL url = getURL();
			checkedUri = new URI(url.getProtocol(), url.getUserInfo(), url
					.getHost(), url.getPort(), url.getPath(), url.getQuery(),
					url.getRef());
			return checkedUri;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		cachedUrl = null;
	}

	public Date getWhen() {
		return when;
	}

	public void setWhen(Date d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		when = d;
	}

	final static SimpleDateFormat getWhenDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	final static DecimalFormat getContentLengthFormat() {
		return new DecimalFormat("00000000000000");
	}

	public void setWhen(String d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		try {
			when = getWhenDateFormat().parse(d);
		} catch (ParseException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			setWhenNow();
		}
	}

	public void setWhenNow() {
		setWhen(new Date(System.currentTimeMillis()));
	}

	public String getCount() {
		return Integer.toString(count);
	}

	public boolean isStatusFull() {
		return fetchStatus == FetchStatus.FETCHED
				&& parserStatus == ParserStatus.PARSED
				&& indexStatus == IndexStatus.INDEXED;
	}

	public void populate(IndexDocument indexDocument)
			throws MalformedURLException {
		indexDocument.set("url", getUrl());
		indexDocument.set("urlSplit", getUrl());
		indexDocument.set("when", getWhenDateFormat().format(when));
		URL url = getURL();
		if (url != null)
			indexDocument.set("host", url.getHost());
		if (responseCode != null)
			indexDocument.set("responseCode", responseCode);
		if (contentBaseType != null)
			indexDocument.set("contentBaseType", contentBaseType);
		if (contentTypeCharset != null)
			indexDocument.set("contentTypeCharset", contentTypeCharset);
		if (contentLength != null)
			indexDocument.set("contentLength", getContentLengthFormat().format(
					contentLength));
		if (contentEncoding != null)
			indexDocument.set("contentEncoding", contentEncoding);
		if (metaDescription != null)
			indexDocument.set("metaDescription", metaDescription);
		if (metaKeywords != null)
			indexDocument.set("metaKeywords", metaKeywords);
		if (lang != null)
			indexDocument.set("lang", lang);
		if (langMethod != null)
			indexDocument.set("langMethod", langMethod);
		indexDocument.set("robotsTxtStatus", robotsTxtStatus.value);
		indexDocument.set("fetchStatus", fetchStatus.value);
		indexDocument.set("parserStatus", parserStatus.value);
		indexDocument.set("indexStatus", indexStatus.value);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getLangMethod() {
		return langMethod;
	}

	public String getFullLang() {
		StringBuffer sb = new StringBuffer();
		if (lang != null)
			sb.append(lang);
		if (langMethod != null) {
			sb.append('(');
			sb.append(langMethod);
			sb.append(')');
		}
		return sb.toString();
	}

	public void setLangMethod(String langMethod) {
		this.langMethod = langMethod;
	}

}
