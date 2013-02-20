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
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;

public class HttpDownloader extends HttpAbstract {

	public HttpDownloader(String userAgent, boolean bFollowRedirect,
			ProxyHandler proxyHandler) {
		super(userAgent, bFollowRedirect, proxyHandler);
	}

	private void addHeader(HttpUriRequest httpUriRequest,
			List<Header> additionalHeaders) {
		if (additionalHeaders == null)
			return;
		for (Header header : additionalHeaders)
			httpUriRequest.addHeader(header);
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem,
			List<Header> additionalHeaders) throws ClientProtocolException,
			IOException {
		synchronized (this) {
			reset();
			HttpGet httpGet = new HttpGet(uri);
			addHeader(httpGet, additionalHeaders);
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
			downloadItem.setHeaders(getHeaders());
			return downloadItem;
		}
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException {
		return get(uri, credentialItem, null);
	}

	public void head(URI uri, CredentialItem credentialItem,
			List<Header> additionalHeaders) throws ClientProtocolException,
			IOException {
		synchronized (this) {
			reset();
			HttpHead httpHead = new HttpHead(uri);
			addHeader(httpHead, additionalHeaders);
			execute(httpHead, credentialItem);
		}
	}

	public void head(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException {
		head(uri, credentialItem, null);
	}
}
