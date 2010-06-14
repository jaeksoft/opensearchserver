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

import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
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
	private Long contentLength;
	private String contentEncoding;
	private String lang;
	private String langMethod;
	private URL cachedUrl;
	private URI checkedUri;
	private String host;
	private Date when;
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
		setUrl(doc.getValue(UrlItemFieldEnum.url.name(), 0));
		setHost(doc.getValue(UrlItemFieldEnum.host.name(), 0));
		setContentBaseType(doc.getValue(
				UrlItemFieldEnum.contentBaseType.name(), 0));
		setContentTypeCharset(doc.getValue(UrlItemFieldEnum.contentTypeCharset
				.name(), 0));
		setContentLength(doc.getValue(UrlItemFieldEnum.contentLength.name(), 0));
		setContentEncoding(doc.getValue(
				UrlItemFieldEnum.contentEncoding.name(), 0));
		setLang(doc.getValue(UrlItemFieldEnum.lang.name(), 0));
		setLangMethod(doc.getValue(UrlItemFieldEnum.langMethod.name(), 0));
		setWhen(doc.getValue(UrlItemFieldEnum.when.name(), 0));
		setRobotsTxtStatusInt(doc.getValue(UrlItemFieldEnum.robotsTxtStatus
				.name(), 0));
		setFetchStatusInt(doc.getValue(UrlItemFieldEnum.fetchStatus.name(), 0));
		setResponseCode(doc.getValue(UrlItemFieldEnum.responseCode.name(), 0));
		setParserStatusInt(doc
				.getValue(UrlItemFieldEnum.parserStatus.name(), 0));
		setIndexStatusInt(doc.getValue(UrlItemFieldEnum.indexStatus.name(), 0));
	}

	public UrlItem(String sUrl) {
		this();
		setUrl(sUrl);
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
			contentLength = getContentLengthFormat().parse(v).longValue();
		} catch (ParseException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	public void setContentLength(Long v) {
		contentLength = v;

	}

	public Long getContentLength() {
		return contentLength;
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
		indexDocument.set(UrlItemFieldEnum.url.name(), getUrl());
		indexDocument.set(UrlItemFieldEnum.when.name(), getWhenDateFormat()
				.format(when));
		URL url = getURL();
		if (url != null)
			indexDocument.set(UrlItemFieldEnum.host.name(), url.getHost());
		if (responseCode != null)
			indexDocument.set(UrlItemFieldEnum.responseCode.name(),
					responseCode);
		if (contentBaseType != null)
			indexDocument.set(UrlItemFieldEnum.contentBaseType.name(),
					contentBaseType);
		if (contentTypeCharset != null)
			indexDocument.set(UrlItemFieldEnum.contentTypeCharset.name(),
					contentTypeCharset);
		if (contentLength != null)
			indexDocument.set(UrlItemFieldEnum.contentLength.name(),
					getContentLengthFormat().format(contentLength));
		if (contentEncoding != null)
			indexDocument.set(UrlItemFieldEnum.contentEncoding.name(),
					contentEncoding);
		if (lang != null)
			indexDocument.set(UrlItemFieldEnum.lang.name(), lang);
		if (langMethod != null)
			indexDocument.set(UrlItemFieldEnum.langMethod.name(), langMethod);
		indexDocument.set(UrlItemFieldEnum.robotsTxtStatus.name(),
				robotsTxtStatus.value);
		indexDocument.set(UrlItemFieldEnum.fetchStatus.name(),
				fetchStatus.value);
		indexDocument.set(UrlItemFieldEnum.parserStatus.name(),
				parserStatus.value);
		indexDocument.set(UrlItemFieldEnum.indexStatus.name(),
				indexStatus.value);
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

	public TargetStatus getTargetResult() {
		if (robotsTxtStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return robotsTxtStatus.targetStatus;
		if (fetchStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return fetchStatus.targetStatus;
		if (parserStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return parserStatus.targetStatus;
		return indexStatus.targetStatus;
	}

}
