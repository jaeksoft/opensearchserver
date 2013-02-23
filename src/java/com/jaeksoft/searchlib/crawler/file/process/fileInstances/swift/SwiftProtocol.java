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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class SwiftProtocol {

	public static class ObjectMeta {

		public final String path;
		public final Long lastModified;
		public final String contentType;
		public final Long contentLength;

		private ObjectMeta(String path, DownloadItem downloadItem) {
			this.path = path;
			lastModified = downloadItem.getLastModified();
			contentType = downloadItem.getContentBaseType();
			contentLength = downloadItem.getContentLength();
		}

		public boolean isDirectory() {
			if (contentType == null)
				return false;
			return "application/directory".equals(contentType);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(path);
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
			SwiftToken swiftToken, String path, boolean withDirectory,
			boolean ignoreHiddenFiles) throws URISyntaxException,
			ClientProtocolException, IOException, SearchLibException {

		List<Header> headerList = new ArrayList<Header>(0);
		swiftToken.putAuthTokenHeader(headerList);

		URI uri = swiftToken.getURI(path, "delimiter=/");
		System.out.println("SwiftProtocol:listOjbects: " + uri.toString());
		DownloadItem downloadItem = httpDownloader.get(uri, null, headerList);
		downloadItem.checkNoError(200, 204);
		InputStream is = downloadItem.getContentInputStream();
		List<String> objectPaths = IOUtils.readLines(is, "UTF-8");
		if (path == null)
			return null;
		List<ObjectMeta> objectMetaList = new ArrayList<ObjectMeta>(0);
		for (String objectPath : objectPaths) {
			// Ignore path which ends with separators
			if (objectPath.endsWith("/"))
				continue;
			if (ignoreHiddenFiles)
				if (objectPath.startsWith("."))
					continue;
			uri = swiftToken.getURI(path + '/' + objectPath, null);
			System.out.println("SwiftProtocol:metas: " + uri.toString());
			downloadItem = httpDownloader.head(uri, null, headerList);
			downloadItem.checkNoError(200, 204);
			ObjectMeta objectMeta = new ObjectMeta(objectPath, downloadItem);
			if (!withDirectory && objectMeta.isDirectory())
				continue;
			objectMetaList.add(objectMeta);
		}
		return objectMetaList;
	}

	public static InputStream getObject(HttpDownloader downloader, String path,
			SwiftToken swiftToken, ObjectMeta object)
			throws URISyntaxException, ClientProtocolException,
			IllegalStateException, IOException, SearchLibException {
		List<Header> headerList = new ArrayList<Header>(0);
		swiftToken.putAuthTokenHeader(headerList);
		URI uri = swiftToken.getURI(path + '/' + object.path, null);
		System.out.println("SwiftProtocol:get: " + uri.toString());
		DownloadItem downloadItem = downloader.get(uri, null, headerList);
		downloadItem.checkNoError(200, 204);
		return downloadItem.getContentInputStream();
	}
}
