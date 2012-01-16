/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

public class DownloadItem {

	private URI uri;
	private URI redirectLocation = null;
	private Long contentLength = null;
	private String contentDispositionFilename = null;
	private String contentBaseType = null;
	private String contentTypeCharset = null;
	private String contentEncoding = null;
	private Integer statusCode = null;
	private InputStream contentInputStream = null;

	public DownloadItem(URI uri) {
		this.uri = uri;
	}

	protected final static String KEY_REDIRECT_LOCATION = "KEY_REDIRECT_LOCATION";
	protected final static String KEY_CONTENT_DISPOSITION_FILENAME = "KEY_CONTENT_DISPOSITION_FILENAME";
	protected final static String KEY_CONTENT_LENGTH = "KEY_CONTENT_LENGTH";
	protected final static String KEY_CONTENT_BASE_TYPE = "KEY_CONTENT_BASE_TYPE";
	protected final static String KEY_CONTENT_TYPE_CHARSET = "KEY_CONTENT_TYPE_CHARSET";
	protected final static String KEY_CONTENT_ENCODING = "KEY_CONTENT_ENCODING";
	protected final static String KEY_STATUS_CODE = "KEY_STATUS_CODE";

	public String getMetaAsJson() throws JSONException {
		JSONObject json = new JSONObject();

		if (redirectLocation != null)
			json.put(KEY_REDIRECT_LOCATION, redirectLocation.toASCIIString());

		if (contentLength != null)
			json.put(KEY_CONTENT_LENGTH, contentLength);

		if (contentDispositionFilename != null)
			json.put(KEY_CONTENT_DISPOSITION_FILENAME,
					contentDispositionFilename);

		if (contentBaseType != null)
			json.put(KEY_CONTENT_BASE_TYPE, contentBaseType);

		if (contentTypeCharset != null)
			json.put(KEY_CONTENT_TYPE_CHARSET, contentTypeCharset);

		if (contentEncoding != null)
			json.put(KEY_CONTENT_ENCODING, contentEncoding);

		if (statusCode != null)
			json.put(KEY_STATUS_CODE, statusCode);

		return json.toString();
	}

	public void loadMetaFromJson(org.json.JSONObject json)
			throws URISyntaxException, JSONException {

		if (json.has(KEY_REDIRECT_LOCATION)) {
			String s = json.getString(KEY_REDIRECT_LOCATION);
			if (s != null)
				redirectLocation = new URI(s);
		}
		if (json.has(KEY_CONTENT_LENGTH))
			contentLength = json.getLong(KEY_CONTENT_LENGTH);

		if (json.has(KEY_CONTENT_DISPOSITION_FILENAME))
			contentDispositionFilename = json
					.getString(KEY_CONTENT_DISPOSITION_FILENAME);

		if (json.has(KEY_CONTENT_BASE_TYPE))
			contentBaseType = json.getString(KEY_CONTENT_BASE_TYPE);

		if (json.has(KEY_CONTENT_TYPE_CHARSET))
			contentBaseType = json.getString(KEY_CONTENT_TYPE_CHARSET);

		if (json.has(KEY_CONTENT_ENCODING))
			contentEncoding = json.getString(KEY_CONTENT_ENCODING);

		if (json.has(KEY_STATUS_CODE))
			statusCode = json.getInt(KEY_STATUS_CODE);

	}

	/**
	 * @return the redirectLocation
	 */
	public URI getRedirectLocation() {
		return redirectLocation;
	}

	/**
	 * @param redirectLocation
	 *            the redirectLocation to set
	 */
	public void setRedirectLocation(URI redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	/**
	 * @return the contentLength
	 */
	public Long getContentLength() {
		return contentLength;
	}

	/**
	 * @param contentLength
	 *            the contentLength to set
	 */
	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the contentDispositionFilename
	 */
	public String getContentDispositionFilename() {
		return contentDispositionFilename;
	}

	/**
	 * @param contentDispositionFilename
	 *            the contentDispositionFilename to set
	 */
	public void setContentDispositionFilename(String contentDispositionFilename) {
		this.contentDispositionFilename = contentDispositionFilename;
	}

	/**
	 * @return the contentBaseType
	 */
	public String getContentBaseType() {
		return contentBaseType;
	}

	/**
	 * @param contentBaseType
	 *            the contentBaseType to set
	 */
	public void setContentBaseType(String contentBaseType) {
		this.contentBaseType = contentBaseType;
	}

	/**
	 * @return the contentTypeCharset
	 */
	public String getContentTypeCharset() {
		return contentTypeCharset;
	}

	/**
	 * @param contentTypeCharset
	 *            the contentTypeCharset to set
	 */
	public void setContentTypeCharset(String contentTypeCharset) {
		this.contentTypeCharset = contentTypeCharset;
	}

	/**
	 * @return the contentEncoding
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	/**
	 * @param contentEncoding
	 *            the contentEncoding to set
	 */
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	/**
	 * @return the statusCode
	 */
	public Integer getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the contentInputStream
	 */
	public InputStream getContentInputStream() {
		return contentInputStream;
	}

	/**
	 * @param contentInputStream
	 *            the inputStream to set
	 */
	public void setContentInputStream(InputStream contentInputStream) {
		this.contentInputStream = contentInputStream;
	}

	/**
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}

}
