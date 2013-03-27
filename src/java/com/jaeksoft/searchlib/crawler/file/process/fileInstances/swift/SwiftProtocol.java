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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SwiftProtocol {

	private final static String APPLICATION_DIRECTORY = "application/directory";

	// 2013-02-23T10:53:26.184120
	private final static SimpleDateFormat swiftDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

	public static class ObjectMeta {

		public final String pathName;
		public final String name;
		public final Long lastModified;
		public final String contentType;
		public final Long contentLength;
		public final boolean isDirectory;
		public final boolean isHidden;

		private ObjectMeta(JSONObject json) throws JSONException,
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

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(pathName);
			sb.append(' ');
			sb.append(contentType);
			sb.append(' ');
			sb.append(contentLength);
			sb.append(' ');
			sb.append(lastModified);
			return sb.toString();
		}
	}

	public static List<ObjectMeta> listObjects(HttpDownloader httpDownloader,
			SwiftToken swiftToken, String container, String path,
			boolean withDirectory, boolean ignoreHiddenFiles)
			throws URISyntaxException, ClientProtocolException, IOException,
			SearchLibException, JSONException, ParseException {

		List<Header> headerList = new ArrayList<Header>(0);
		swiftToken.putAuthTokenHeader(headerList);

		URI uri = swiftToken.getURI(container, path, true);
		DownloadItem downloadItem = httpDownloader.get(uri, null, headerList);
		downloadItem.checkNoError(200, 204);

		String jsonString = downloadItem.getContentAsString();
		JSONArray jsonArray = new JSONArray(jsonString);
		List<ObjectMeta> objectMetaList = new ArrayList<ObjectMeta>(0);
		for (int i = 0; i < jsonArray.length(); i++) {
			ObjectMeta objectMeta = new ObjectMeta(jsonArray.getJSONObject(i));
			if (ignoreHiddenFiles && objectMeta.isHidden)
				continue;
			if (!withDirectory && objectMeta.isDirectory)
				continue;
			if (path.equals(objectMeta.pathName))
				continue;
			objectMetaList.add(objectMeta);
		}
		return objectMetaList;
	}

	public static InputStream getObject(HttpDownloader downloader,
			String container, SwiftToken swiftToken, ObjectMeta object)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		List<Header> headerList = new ArrayList<Header>(0);
		swiftToken.putAuthTokenHeader(headerList);
		URI uri = swiftToken.getURI(container, object.pathName, false);
		System.out.println("SwiftProtocol:get: " + uri.toString());
		DownloadItem downloadItem = downloader.get(uri, null, headerList);
		downloadItem.checkNoError(200, 204);
		return downloadItem.getContentInputStream();
	}
}
