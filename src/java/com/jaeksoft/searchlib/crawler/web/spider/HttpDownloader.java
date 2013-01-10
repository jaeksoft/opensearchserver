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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;

import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;

public class HttpDownloader extends HttpAbstract {

	public HttpDownloader(String userAgent, boolean bFollowRedirect,
			ProxyHandler proxyHandler) {
		super(userAgent, bFollowRedirect, proxyHandler);
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException {
		synchronized (this) {
			reset();
			HttpGet httpGet = new HttpGet(uri);
			execute(httpGet, credentialItem);

			DownloadItem downloadItem = new DownloadItem(uri);
			downloadItem.setRedirectLocation(getRedirectLocation());
			downloadItem.setContentLength(getContentLength());
			downloadItem
					.setContentDispositionFilename(getContentDispositionFilename());
			downloadItem.setContentBaseType(getContentBaseType());
			downloadItem.setContentEncoding(getContentEncoding());
			downloadItem.setContentTypeCharset(getContentTypeCharset());
			downloadItem.setStatusCode(getStatusCode());
			downloadItem.setContentInputStream(getContent());
			return downloadItem;
		}
	}

	public void head(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException {
		synchronized (this) {
			reset();
			HttpHead httpHead = new HttpHead(uri);
			execute(httpHead, credentialItem);
		}
	}

}
