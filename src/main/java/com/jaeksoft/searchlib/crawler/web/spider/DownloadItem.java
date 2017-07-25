/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.SearchLibException.WrongStatusCodeException;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;

public class DownloadItem {

	private final URI uri;
	private final long crawlTime;
	private final boolean fromCache;

	private URI redirectLocation = null;
	private Long contentLength = null;
	private String contentDispositionFilename = null;
	private String contentBaseType = null;
	private String contentTypeCharset = null;
	private String contentEncoding = null;
	private String contentLocation = null;
	private Long lastModified = null;
	private Integer statusCode = null;
	private String reasonPhrase = null;
	private InputStream contentInputStream = null;
	private List<String> headers = null;
	private Header[] httpHeaders = null;

	public DownloadItem(URI uri, long crawlTime, boolean fromCache) {
		this.uri = uri;
		this.crawlTime = crawlTime;
		this.fromCache = fromCache;
	}

	protected final static String KEY_CRAWL_TIME = "KEY_CRAWL_TIME";
	protected final static String KEY_REDIRECT_LOCATION = "KEY_REDIRECT_LOCATION";
	protected final static String KEY_CONTENT_DISPOSITION_FILENAME = "KEY_CONTENT_DISPOSITION_FILENAME";
	protected final static String KEY_CONTENT_LENGTH = "KEY_CONTENT_LENGTH";
	protected final static String KEY_LAST_MODIFIED = "KEY_LAST_MODIFIED";
	protected final static String KEY_CONTENT_BASE_TYPE = "KEY_CONTENT_BASE_TYPE";
	protected final static String KEY_CONTENT_TYPE_CHARSET = "KEY_CONTENT_TYPE_CHARSET";
	protected final static String KEY_CONTENT_ENCODING = "KEY_CONTENT_ENCODING";
	protected final static String KEY_CONTENT_LOCATION = "KEY_CONTENT_LOCATION";
	protected final static String KEY_STATUS_CODE = "KEY_STATUS_CODE";
	protected final static String KEY_REASON_PHRASE = "KEY_REASON_PHRASE";
	protected final static String KEY_HEADERS = "KEY_HEADERS";

	public String getMetaAsJson() throws JSONException {
		JSONObject json = new JSONObject();

		json.put(KEY_CRAWL_TIME, crawlTime);

		if (redirectLocation != null)
			json.put(KEY_REDIRECT_LOCATION, redirectLocation.toASCIIString());

		if (contentLength != null)
			json.put(KEY_CONTENT_LENGTH, contentLength);

		if (lastModified != null)
			json.put(KEY_LAST_MODIFIED, lastModified);

		if (contentDispositionFilename != null)
			json.put(KEY_CONTENT_DISPOSITION_FILENAME, contentDispositionFilename);

		if (contentBaseType != null)
			json.put(KEY_CONTENT_BASE_TYPE, contentBaseType);

		if (contentTypeCharset != null)
			json.put(KEY_CONTENT_TYPE_CHARSET, contentTypeCharset);

		if (contentEncoding != null)
			json.put(KEY_CONTENT_ENCODING, contentEncoding);

		if (contentLocation != null)
			json.put(KEY_CONTENT_LOCATION, contentLocation);

		if (statusCode != null)
			json.put(KEY_STATUS_CODE, statusCode);

		if (reasonPhrase != null)
			json.put(KEY_REASON_PHRASE, reasonPhrase);

		if (headers != null)
			json.put(KEY_HEADERS, headers);

		return json.toString();
	}

	public DownloadItem(URI uri, org.json.JSONObject json) throws URISyntaxException, JSONException {
		this(uri, json.has(KEY_CRAWL_TIME) ? json.getLong(KEY_CRAWL_TIME) : System.currentTimeMillis(), true);

		if (json.has(KEY_REDIRECT_LOCATION)) {
			String s = json.getString(KEY_REDIRECT_LOCATION);
			if (s != null)
				redirectLocation = new URI(s);
		}
		if (json.has(KEY_CONTENT_LENGTH))
			contentLength = json.getLong(KEY_CONTENT_LENGTH);

		if (json.has(KEY_LAST_MODIFIED))
			lastModified = json.getLong(KEY_LAST_MODIFIED);

		if (json.has(KEY_CONTENT_DISPOSITION_FILENAME))
			contentDispositionFilename = json.getString(KEY_CONTENT_DISPOSITION_FILENAME);

		if (json.has(KEY_CONTENT_BASE_TYPE))
			contentBaseType = json.getString(KEY_CONTENT_BASE_TYPE);

		if (json.has(KEY_CONTENT_TYPE_CHARSET))
			contentTypeCharset = json.getString(KEY_CONTENT_TYPE_CHARSET);

		if (json.has(KEY_CONTENT_ENCODING))
			contentEncoding = json.getString(KEY_CONTENT_ENCODING);

		if (json.has(KEY_CONTENT_LOCATION))
			contentLocation = json.getString(KEY_CONTENT_LOCATION);

		if (json.has(KEY_STATUS_CODE))
			statusCode = json.getInt(KEY_STATUS_CODE);

		if (json.has(KEY_REASON_PHRASE))
			reasonPhrase = json.getString(KEY_REASON_PHRASE);

		if (json.has(KEY_HEADERS)) {
			headers = new ArrayList<>();
			JSONArray headerJsonArray = json.getJSONArray(KEY_HEADERS);
			if (headerJsonArray != null)
				for (int i = 0; i < headerJsonArray.length(); i++)
					headers.add(headerJsonArray.get(i).toString());
		}
	}

	public DownloadItem(URI uri, Document doc) throws URISyntaxException, JSONException {
		this(uri, ((Number) doc.getOrDefault(KEY_CRAWL_TIME, System.currentTimeMillis())).longValue(), true);

		if (doc.containsKey(KEY_REDIRECT_LOCATION))
			redirectLocation = new URI(doc.get(KEY_REDIRECT_LOCATION).toString());

		if (doc.containsKey(KEY_CONTENT_LENGTH))
			contentLength = ((Number) doc.get(KEY_CONTENT_LENGTH)).longValue();

		if (doc.containsKey(KEY_LAST_MODIFIED))
			lastModified = ((Number) doc.get(KEY_LAST_MODIFIED)).longValue();

		if (doc.containsKey(KEY_CONTENT_DISPOSITION_FILENAME))
			contentDispositionFilename = doc.getString(KEY_CONTENT_DISPOSITION_FILENAME);

		if (doc.containsKey(KEY_CONTENT_BASE_TYPE))
			contentBaseType = doc.getString(KEY_CONTENT_BASE_TYPE);

		if (doc.containsKey(KEY_CONTENT_TYPE_CHARSET))
			contentTypeCharset = doc.getString(KEY_CONTENT_TYPE_CHARSET);

		if (doc.containsKey(KEY_CONTENT_ENCODING))
			contentEncoding = doc.getString(KEY_CONTENT_ENCODING);

		if (doc.containsKey(KEY_CONTENT_LOCATION))
			contentLocation = doc.getString(KEY_CONTENT_LOCATION);

		if (doc.containsKey(KEY_STATUS_CODE))
			statusCode = ((Number) doc.get(KEY_STATUS_CODE)).intValue();

		if (doc.containsKey(KEY_REASON_PHRASE))
			reasonPhrase = doc.getString(KEY_REASON_PHRASE);

		if (doc.containsKey(KEY_HEADERS)) {
			headers = new ArrayList<>();
			((Collection<Object>) doc.get(KEY_HEADERS)).forEach(value -> headers.add(value.toString()));
		}
	}

	/**
	 * @return the redirectLocation
	 */
	public URI getRedirectLocation() {
		return redirectLocation;
	}

	/**
	 * @param redirectLocation the redirectLocation to set
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
	 * @return the lastModified
	 */
	public Long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @param contentLength the contentLength to set
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
	 * @param contentDispositionFilename the contentDispositionFilename to set
	 */
	public void setContentDispositionFilename(String contentDispositionFilename) {
		this.contentDispositionFilename = contentDispositionFilename;
	}

	public String getFileName() throws MalformedURLException {
		if (contentDispositionFilename != null)
			return contentDispositionFilename;
		if (uri == null)
			return null;
		String urlFile = uri.toURL().getPath();
		if (urlFile == null)
			return null;
		return FilenameUtils.getName(urlFile);
	}

	/**
	 * @return the contentBaseType
	 */
	public String getContentBaseType() {
		return contentBaseType;
	}

	/**
	 * @param contentBaseType the contentBaseType to set
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
	 * @param contentTypeCharset the contentTypeCharset to set
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
	 * @param contentEncoding the contentEncoding to set
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

	public void checkNoErrorRange(int fromInclusive, int toExclusive) throws WrongStatusCodeException {
		if (statusCode == null)
			throw new WrongStatusCodeException("No status code - ", uri);
		if (statusCode < fromInclusive || statusCode >= toExclusive)
			throw new WrongStatusCodeException("Wrong status code: ", statusCode, ' ', reasonPhrase, " - ", uri);
	}

	public void checkNoErrorList(int... validCodes) throws WrongStatusCodeException {
		if (statusCode == null)
			throw new WrongStatusCodeException("Wrong status code: ", statusCode, ' ', reasonPhrase, " - ", uri);
		for (int validCode : validCodes)
			if (statusCode == validCode)
				return;
		throw new WrongStatusCodeException("Wrong status code: ", statusCode, ' ', reasonPhrase, " - ", uri);
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the reasonPhrase
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * @param reasonPhrase the reasonPhrase to set
	 */
	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * @return the contentInputStream
	 */
	public InputStream getContentInputStream() {
		return contentInputStream;
	}

	/**
	 * @param contentInputStream the inputStream to set
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

	/**
	 * @return the fromCache
	 */
	public boolean isFromCache() {
		return fromCache;
	}

	/**
	 * @return the time when the content has been crawled
	 */
	public long getCrawlTime() {
		return crawlTime;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		httpHeaders = headers;
		if (headers == null)
			return;
		this.headers = new ArrayList<>(headers.length);
		for (Header header : headers) {
			StringBuilder sb = new StringBuilder();
			sb.append(header.getName());
			sb.append(": ");
			sb.append(header.getValue());
			this.headers.add(sb.toString());
		}
	}

	public String getFirstHttpHeader(String name) {
		if (httpHeaders == null)
			return null;
		for (Header header : httpHeaders)
			if (header.getName().equalsIgnoreCase(name))
				return header.getValue();
		return null;
	}

	public String getContentAsString() throws IOException {
		if (contentInputStream == null)
			return null;
		return IOUtils.toString(contentInputStream, Charset.defaultCharset());
	}

	/**
	 * @return the contentLocation
	 */
	public String getContentLocation() {
		return contentLocation;
	}

	/**
	 * @param contentLocation the contentLocation to set
	 */
	public void setContentLocation(String contentLocation) {
		this.contentLocation = contentLocation;
	}

	public void writeToFile(File file) throws IOException {
		if (contentInputStream == null)
			return;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			IOUtils.copy(contentInputStream, bos);
		} finally {
			IOUtils.close(bos, fos);
		}
	}

	public void writeToZip(ZipArchiveOutputStream zipOutput) throws IOException {
		if (contentInputStream == null)
			return;
		String[] domainParts = StringUtils.split(uri.getHost(), '.');
		StringBuilder path = new StringBuilder();
		for (int i = domainParts.length - 1; i >= 0; i--) {
			path.append(domainParts[i]);
			path.append('/');
		}
		String[] pathParts = StringUtils.split(uri.getPath(), '/');
		for (int i = 0; i < pathParts.length - 1; i++) {
			if (StringUtils.isEmpty(pathParts[i]))
				continue;
			path.append(pathParts[i]);
			path.append('/');
		}
		if (contentDispositionFilename != null)
			path.append(contentDispositionFilename);
		else {
			String lastPart = pathParts == null || pathParts.length == 0 ? null : pathParts[pathParts.length - 1];
			if (StringUtils.isEmpty(lastPart))
				path.append("index");
			else
				path.append(lastPart);
		}
		if (uri.getPath().endsWith("/"))
			path.append("/_index");
		String query = uri.getQuery();
		String fragment = uri.getFragment();
		if (!StringUtils.isEmpty(query) || !StringUtils.isEmpty(fragment)) {
			CRC32 crc32 = new CRC32();
			if (!StringUtils.isEmpty(query))
				crc32.update(query.getBytes());
			if (!StringUtils.isEmpty(fragment))
				crc32.update(fragment.getBytes());
			path.append('.');
			path.append(crc32.getValue());
		}
		ZipArchiveEntry zipEntry = new ZipArchiveEntry(path.toString());
		zipOutput.putArchiveEntry(zipEntry);
		BufferedInputStream bis = null;
		byte[] buffer = new byte[65536];
		try {
			bis = new BufferedInputStream(contentInputStream);
			int l;
			while ((l = bis.read(buffer)) != -1)
				zipOutput.write(buffer, 0, l);
			zipOutput.closeArchiveEntry();
		} finally {
			IOUtils.close(bis);
		}
	}
}
