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

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class SwiftListObjects {

	private final List<String> objectList;

	public SwiftListObjects(HttpDownloader httpDownloader, SwiftToken swiftToken)
			throws URISyntaxException, ClientProtocolException, IOException {

		List<Header> headerList = new ArrayList<Header>(0);
		swiftToken.putAuthTokenHeader(headerList);
		URI uri = new URI(swiftToken.getStorageUrl());
		DownloadItem downloadItem = httpDownloader.get(uri, null, headerList);
		InputStream is = downloadItem.getContentInputStream();
		objectList = IOUtils.readLines(is, "UTF-8");
	}

	public List<String> getObjectList() {
		return objectList;
	}
}
