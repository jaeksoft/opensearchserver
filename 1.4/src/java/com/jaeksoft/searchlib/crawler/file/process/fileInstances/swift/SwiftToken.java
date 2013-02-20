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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicHeader;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class SwiftToken {

	private static final String X_Auth_User = "X-Auth-User";
	private static final String X_Auth_Key = "X-Auth-Key";
	private static final String X_Storage_Url = "X-Storage-Url";
	public static final String X_Auth_Token = "X-Auth-Token";

	private final String storageUrl;
	private final String authToken;

	public SwiftToken(HttpDownloader httpDownloader, String authUrl,
			String authUser, String authKey) throws URISyntaxException,
			ClientProtocolException, IOException {

		List<Header> headerList = new ArrayList<Header>(0);
		headerList.add(new BasicHeader(X_Auth_User, authUser));
		headerList.add(new BasicHeader(X_Auth_Key, authKey));
		URI uri = new URI("http", authUser, null, null);
		DownloadItem downloadItem = httpDownloader.get(uri, null, headerList);
		storageUrl = downloadItem.getFirstHttpHeader(X_Storage_Url);
		if (storageUrl == null)
			throw new IOException("Authentication failed: no storage url given");
		authToken = downloadItem.getFirstHttpHeader(X_Auth_Token);
		if (authToken == null)
			throw new IOException("Authentication failed: no auth token given");
	}

	public void putAuthTokenHeader(List<Header> headerList) {
		headerList.add(new BasicHeader(X_Auth_Token, authToken));
	}

	public String getStorageUrl() {
		return storageUrl;
	}

}
