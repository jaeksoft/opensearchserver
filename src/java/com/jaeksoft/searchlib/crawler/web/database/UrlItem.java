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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class UrlItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043010587042224473L;

	private String url;
	private String contentDispositionFilename;
	private String contentBaseType;
	private String contentTypeCharset;
	private Long contentLength;
	private String contentEncoding;
	private String lang;
	private String langMethod;
	private URL cachedUrl;
	private URI checkedUri;
	private String host;
	private List<String> subhost;
	private Date when;
	private RobotsTxtStatus robotsTxtStatus;
	private FetchStatus fetchStatus;
	private Integer responseCode;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;
	private int count;
	private String md5size;
	private Date lastModifiedDate;
	private List<String> outLinks;
	private List<String> inLinks;
	private String parentUrl;
	private LinkItem.Origin origin;

	protected UrlItem() {
		url = null;
		cachedUrl = null;
		checkedUri = null;
		contentDispositionFilename = null;
		contentBaseType = null;
		contentTypeCharset = null;
		contentLength = null;
		contentEncoding = null;
		lang = null;
		langMethod = null;
		host = null;
		subhost = null;
		outLinks = null;
		inLinks = null;
		when = new Date();
		robotsTxtStatus = RobotsTxtStatus.UNKNOWN;
		fetchStatus = FetchStatus.UN_FETCHED;
		responseCode = null;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
		count = 0;
		md5size = null;
		lastModifiedDate = null;
		parentUrl = null;
		origin = null;
	}

	protected void init(ResultDocument doc, UrlItemFieldEnum urlItemFieldEnum) {
		setUrl(doc.getValueContent(urlItemFieldEnum.url.getName(), 0));
		setHost(doc.getValueContent(urlItemFieldEnum.host.getName(), 0));
		setSubHost(doc.getValueArray(urlItemFieldEnum.subhost.getName()));
		addOutLinks(doc.getValueArray(urlItemFieldEnum.outlink.getName()));
		addInLinks(doc.getValueArray(urlItemFieldEnum.inlink.getName()));
		setContentDispositionFilename(doc.getValueContent(
				urlItemFieldEnum.contentDispositionFilename.getName(), 0));
		setContentBaseType(doc.getValueContent(
				urlItemFieldEnum.contentBaseType.getName(), 0));
		setContentTypeCharset(doc.getValueContent(
				urlItemFieldEnum.contentTypeCharset.getName(), 0));
		setContentLength(doc.getValueContent(
				urlItemFieldEnum.contentLength.getName(), 0));
		setContentEncoding(doc.getValueContent(
				urlItemFieldEnum.contentEncoding.getName(), 0));
		setLang(doc.getValueContent(urlItemFieldEnum.lang.getName(), 0));
		setLangMethod(doc.getValueContent(
				urlItemFieldEnum.langMethod.getName(), 0));
		setWhen(doc.getValueContent(urlItemFieldEnum.when.getName(), 0));
		setRobotsTxtStatusInt(doc.getValueContent(
				urlItemFieldEnum.robotsTxtStatus.getName(), 0));
		setFetchStatusInt(doc.getValueContent(
				urlItemFieldEnum.fetchStatus.getName(), 0));
		setResponseCode(doc.getValueContent(
				urlItemFieldEnum.responseCode.getName(), 0));
		setParserStatusInt(doc.getValueContent(
				urlItemFieldEnum.parserStatus.getName(), 0));
		setIndexStatusInt(doc.getValueContent(
				urlItemFieldEnum.indexStatus.getName(), 0));
		setMd5size(doc.getValueContent(urlItemFieldEnum.md5size.getName(), 0));
		setLastModifiedDate(doc.getValueContent(
				urlItemFieldEnum.lastModifiedDate.getName(), 0));
		setParentUrl(doc.getValueContent(urlItemFieldEnum.parentUrl.getName(),
				0));
		setOrigin(LinkItem.findOrigin(doc.getValueContent(
				urlItemFieldEnum.origin.getName(), 0)));

	}

	public List<String> getSubHost() {
		return subhost;
	}

	public List<String> getOutLinks() {
		return outLinks;
	}

	public List<String> getInLinks() {
		return inLinks;
	}

	public void setSubHost(FieldValueItem[] subhostlist) {
		this.subhost = null;
		if (subhostlist == null)
			return;
		this.subhost = new ArrayList<String>();
		for (FieldValueItem item : subhostlist)
			this.subhost.add(item.getValue());
	}

	public void clearOutLinks() {
		if (outLinks == null)
			return;
		outLinks.clear();
	}

	public void addOutLinks(FieldValueItem[] linkList) {
		if (linkList == null)
			return;
		if (outLinks == null)
			outLinks = new ArrayList<String>();
		for (FieldValueItem item : linkList)
			outLinks.add(item.getValue());
	}

	public void addOutLinks(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addOutLinks(fieldContent.getValues());
	}

	public void clearInLinks() {
		if (inLinks == null)
			return;
		inLinks.clear();
	}

	public void addInLinks(FieldValueItem[] linkList) {
		if (linkList == null)
			return;
		if (inLinks == null)
			inLinks = new ArrayList<String>();
		for (FieldValueItem item : linkList)
			inLinks.add(item.getValue());
	}

	public void addInLinks(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addInLinks(fieldContent.getValues());
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

	public String getContentDispositionFilename() {
		return contentDispositionFilename;
	}

	public void setContentDispositionFilename(String v) {
		contentDispositionFilename = v;
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
			Logging.error(e.getMessage(), e);
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
			URL url;
			url = getURL();
			checkedUri = new URI(url.getProtocol(), url.getUserInfo(),
					url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return checkedUri;

		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		synchronized (this) {
			this.url = url;
			cachedUrl = null;
		}
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public LinkItem.Origin getOrigin() {
		return origin;
	}

	public void setOrigin(LinkItem.Origin origin) {
		this.origin = origin;
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

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	protected void setLastModifiedDate(String d) {
		try {
			this.lastModifiedDate = d == null ? null : getWhenDateFormat()
					.parse(d);
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	public void setLastModifiedDate(Date d) {
		this.lastModifiedDate = d;
	}

	final static SimpleDateFormat getWhenDateFormat() {
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	final static DecimalFormat getContentLengthFormat() {
		return new DecimalFormat("00000000000000");
	}

	protected void setWhen(String d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		try {
			when = getWhenDateFormat().parse(d);
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
			setWhenNow();
		}

	}

	public void setWhenNow() {
		setWhen(new Date(System.currentTimeMillis()));

	}

	public String getCount() {
		return Integer.toString(count);
	}

	public static List<String> buildSubHost(String host) {
		if (host == null)
			return null;
		List<String> subhost = new ArrayList<String>();
		int lastPos = host.length();
		while (lastPos > 0) {
			lastPos = host.lastIndexOf('.', lastPos - 1);
			if (lastPos == -1)
				break;
			subhost.add(host.substring(lastPos + 1));
		}
		subhost.add(host);
		return subhost;
	}

	public void populate(IndexDocument indexDocument,
			UrlItemFieldEnum urlItemFieldEnum) {
		SimpleDateFormat df = getWhenDateFormat();
		indexDocument.setString(urlItemFieldEnum.url.getName(), getUrl());
		indexDocument.setString(urlItemFieldEnum.when.getName(),
				df.format(when));
		try {
			URL url = getURL();
			if (url != null) {
				indexDocument.setString(urlItemFieldEnum.host.getName(),
						url.getHost());
				indexDocument.setStringList(urlItemFieldEnum.subhost.getName(),
						buildSubHost(url.getHost()));
			}
		} catch (MalformedURLException e) {
			Logging.warn(e);
		}
		if (inLinks != null)
			indexDocument.setStringList(urlItemFieldEnum.inlink.getName(),
					inLinks);
		if (outLinks != null)
			indexDocument.setStringList(urlItemFieldEnum.outlink.getName(),
					outLinks);
		if (responseCode != null)
			indexDocument.setObject(urlItemFieldEnum.responseCode.getName(),
					responseCode);
		if (contentDispositionFilename != null)
			indexDocument.setString(
					urlItemFieldEnum.contentDispositionFilename.getName(),
					contentDispositionFilename);
		if (contentBaseType != null)
			indexDocument.setString(urlItemFieldEnum.contentBaseType.getName(),
					contentBaseType);
		if (contentTypeCharset != null)
			indexDocument.setString(
					urlItemFieldEnum.contentTypeCharset.getName(),
					contentTypeCharset);
		if (contentLength != null)
			indexDocument.setString(urlItemFieldEnum.contentLength.getName(),
					getContentLengthFormat().format(contentLength));
		if (contentEncoding != null)
			indexDocument.setString(urlItemFieldEnum.contentEncoding.getName(),
					contentEncoding);
		if (lang != null)
			indexDocument.setString(urlItemFieldEnum.lang.getName(), lang);
		if (langMethod != null)
			indexDocument.setString(urlItemFieldEnum.langMethod.getName(),
					langMethod);
		indexDocument.setObject(urlItemFieldEnum.robotsTxtStatus.getName(),
				robotsTxtStatus.value);
		indexDocument.setObject(urlItemFieldEnum.fetchStatus.getName(),
				fetchStatus.value);
		indexDocument.setObject(urlItemFieldEnum.parserStatus.getName(),
				parserStatus.value);
		indexDocument.setObject(urlItemFieldEnum.indexStatus.getName(),
				indexStatus.value);
		if (md5size != null)
			indexDocument
					.setString(urlItemFieldEnum.md5size.getName(), md5size);
		if (lastModifiedDate != null)
			indexDocument.setString(
					urlItemFieldEnum.lastModifiedDate.getName(),
					df.format(lastModifiedDate));
		if (parentUrl != null)
			indexDocument.setString(urlItemFieldEnum.parentUrl.getName(),
					parentUrl);
		if (origin != null)
			indexDocument.setString(urlItemFieldEnum.origin.getName(),
					origin.name());
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

	public String getMd5size() {
		return md5size;
	}

	public void setMd5size(String md5size) {
		this.md5size = md5size;
	}

	public boolean isLinkDiscoverable() {
		if (fetchStatus != FetchStatus.FETCHED)
			return false;
		if (indexStatus == IndexStatus.INDEXED)
			return true;
		if (indexStatus == IndexStatus.META_NOINDEX)
			return true;
		return false;
	}
}
