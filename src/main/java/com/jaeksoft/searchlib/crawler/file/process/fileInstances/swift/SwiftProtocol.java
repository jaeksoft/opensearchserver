/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.array.BytesOutputStream;

public class SwiftProtocol {

	private final static String APPLICATION_DIRECTORY = "application/directory";

	// 2013-02-23T10:53:26.184120
	private final static ThreadSafeSimpleDateFormat swiftDateFormat = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

	public static class ObjectMeta {

		public final String pathName;
		public final String name;
		public final Long lastModified;
		public final String contentType;
		public final Long contentLength;
		public final boolean isDirectory;
		public final boolean isHidden;

		private ObjectMeta(final JSONObject json) throws JSONException,
				ParseException {
			if (json.has("subdir")) {
				this.pathName = json.getString("subdir");
				this.isDirectory = true;
				this.lastModified = null;
				this.contentLength = null;
				this.contentType = APPLICATION_DIRECTORY;
			} else {
				this.pathName = json.getString("name");
				this.contentType = json.has("content_type") ? json
						.getString("content_type") : null;
				this.lastModified = json.has("last_modified") ? swiftDateFormat
						.parse(json.getString("last_modified")).getTime()
						: null;
				this.contentLength = json.has("bytes") ? json.getLong("bytes")
						: null;
				this.isDirectory = APPLICATION_DIRECTORY.equals(contentType);
			}
			name = LinkUtils.lastPart(pathName);
			isHidden = name.startsWith(".");
		}

		private ObjectMeta(final String path, final DownloadItem downloadItem) {
			lastModified = downloadItem.getLastModified();
			contentType = downloadItem.getContentBaseType();
			contentLength = downloadItem.getContentLength();
			pathName = path;
			isDirectory = APPLICATION_DIRECTORY.equals(contentType);
			isHidden = false;
			name = null;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(pathName);
			sb.append(' ');
			sb.append(contentType);
			sb.append(' ');
			sb.append(contentLength);
			sb.append(' ');
			sb.append(lastModified);
			return sb.toString();
		}
	}

	final public static List<ObjectMeta> listObjects(
			HttpDownloader httpDownloader, SwiftToken swiftToken,
			String container, String path, boolean withDirectory,
			boolean ignoreHiddenFiles, Matcher[] exclusionMatchers)
			throws URISyntaxException, ClientProtocolException, IOException,
			SearchLibException, JSONException, ParseException {

		final List<Header> headerList = swiftToken.getAuthTokenHeader(null);
		final URI uri = swiftToken.getURI(container, path, true);
		final DownloadItem downloadItem = httpDownloader.get(uri, null,
				headerList, null);
		downloadItem.checkNoErrorRange(200, 204);

		final String jsonString = downloadItem.getContentAsString();
		final JSONArray jsonArray = new JSONArray(jsonString);
		List<ObjectMeta> objectMetaList = new ArrayList<ObjectMeta>(0);
		for (int i = 0; i < jsonArray.length(); i++) {
			ObjectMeta objectMeta = new ObjectMeta(jsonArray.getJSONObject(i));
			if (ignoreHiddenFiles && objectMeta.isHidden)
				continue;
			if (!withDirectory && objectMeta.isDirectory)
				continue;
			if (path.equals(objectMeta.pathName))
				continue;
			if (exclusionMatchers != null)
				if (RegExpUtils.find(objectMeta.pathName, exclusionMatchers))
					continue;
			objectMetaList.add(objectMeta);
		}
		return objectMetaList;
	}

	final public static List<String> listObjects(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container) throws URISyntaxException,
			ClientProtocolException, IllegalStateException, IOException,
			SearchLibException {
		final List<Header> headerList = swiftToken.getAuthTokenHeader(null);
		final URI uri = swiftToken.getContainerURI(container);
		DownloadItem downloadItem = httpDownloader.get(uri, null, headerList,
				null);
		downloadItem.checkNoErrorRange(200, 204);
		return IOUtils.readLines(downloadItem.getContentInputStream());
	}

	final public static ObjectMeta headObject(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		final List<Header> headerList = swiftToken.getAuthTokenHeader(null);
		final URI uri = swiftToken.getPathURI(container, path);
		final DownloadItem downloadItem = httpDownloader.head(uri, null,
				headerList, null);
		final Integer statusCode = downloadItem.getStatusCode();
		if (statusCode != null && statusCode == 404)
			return null;
		downloadItem.checkNoErrorRange(200, 300);
		return new ObjectMeta(path, downloadItem);
	}

	final public static InputStream getObject(HttpDownloader downloader,
			String container, SwiftToken swiftToken, ObjectMeta object)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		final List<Header> headerList = swiftToken.getAuthTokenHeader(null);
		final URI uri = swiftToken.getURI(container, object.pathName, false);
		final DownloadItem downloadItem = downloader.get(uri, null, headerList,
				null);
		downloadItem.checkNoErrorRange(200, 204);
		return downloadItem.getContentInputStream();
	}

	final public static void touchObject(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		final List<Header> headerList = swiftToken
				.getAuthTokenHeader(new ArrayList<Header>(2));
		headerList.add(new BasicHeader("Content-Length", "0"));
		final URI uri = swiftToken.getPathURI(container, path);
		final DownloadItem downloadItem = httpDownloader.put(uri, null,
				headerList, null, null);
		downloadItem.checkNoErrorList(201);
	}

	final public static void writeObject(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path,
			BytesOutputStream bytes) throws URISyntaxException,
			ClientProtocolException, IllegalStateException, IOException,
			SearchLibException {
		final List<Header> headerList = swiftToken
				.getAuthTokenHeader(new ArrayList<Header>(2));
		headerList.add(new BasicHeader("Content-Length", Integer.toString(bytes
				.size())));
		final URI uri = swiftToken.getPathURI(container, path);
		final DownloadItem downloadItem = httpDownloader.put(uri, null,
				headerList, null, bytes.getHttpEntity());
		downloadItem.checkNoErrorList(201);
	}

	final public static void deleteObject(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		final List<Header> headerList = swiftToken.getAuthTokenHeader(null);
		final URI uri = swiftToken.getPathURI(container, path);
		final DownloadItem downloadItem = httpDownloader.delete(uri, null,
				headerList, null);
		downloadItem.checkNoErrorList(204, 404);
	}

	final public static InputStream readObject(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path,
			long rangeStart, long rangeEnd) throws URISyntaxException,
			ClientProtocolException, IllegalStateException, IOException,
			SearchLibException {
		final List<Header> headerList = swiftToken
				.getAuthTokenHeader(new ArrayList<Header>(2));
		headerList.add(new BasicHeader("Range", StringUtils.fastConcat(
				Long.toString(rangeStart), '-', Long.toString(rangeEnd))));
		final URI uri = swiftToken.getPathURI(container, path);
		final DownloadItem downloadItem = httpDownloader.get(uri, null,
				headerList, null);
		downloadItem.checkNoErrorList(200, 206);
		return downloadItem.getContentInputStream();
	}

}
