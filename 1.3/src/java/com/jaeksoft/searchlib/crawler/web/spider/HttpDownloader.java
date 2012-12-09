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
import java.io.InputStream;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;

public class HttpDownloader {

	private DefaultHttpClient httpClient = null;
	private HttpGet httpGet = null;
	private HttpContext httpContext = null;
	private HttpResponse httpResponse = null;
	private HttpEntity httpEntity = null;
	private StatusLine statusLine = null;
	private RedirectStrategy redirectStrategy;
	private ProxyHandler proxyHandler;

	public HttpDownloader(String userAgent, boolean bFollowRedirect,
			ProxyHandler proxyHandler) {
		redirectStrategy = new DefaultRedirectStrategy();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		// No more than one 1 minute to establish the connection
		HttpConnectionParams.setConnectionTimeout(params, 1000 * 60);
		// No more than 10 minutes without data
		HttpConnectionParams.setSoTimeout(params, 1000 * 60 * 10);
		// Checking it the connection stale
		HttpConnectionParams.setStaleCheckingEnabled(params, true);
		// paramsBean.setVersion(HttpVersion.HTTP_1_1);
		// paramsBean.setContentCharset("UTF-8");
		paramsBean.setUserAgent(userAgent);
		HttpClientParams.setRedirecting(params, bFollowRedirect);
		httpClient = new DefaultHttpClient(params);
		this.proxyHandler = proxyHandler;
		// TODO RETRY HANDLER ?
	}

	public void release() {
		synchronized (this) {
			try {
				reset();
				httpContext = null;
				if (httpClient != null)
					httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
			}
		}
	}

	private void reset() {
		synchronized (this) {
			if (httpEntity != null) {
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					Logging.warn(e.getMessage(), e);
				}
				httpEntity = null;
			}
			httpGet = null;
			httpResponse = null;
			httpEntity = null;
			statusLine = null;
		}
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException {
		synchronized (this) {
			reset();
			if (proxyHandler != null)
				proxyHandler.check(httpClient, uri);
			CredentialsProvider credential = httpClient
					.getCredentialsProvider();
			if (credentialItem == null)
				credential.clear();
			else
				credential.setCredentials(
						new AuthScope(uri.getHost(), uri.getPort()),
						new UsernamePasswordCredentials(credentialItem
								.getUsername(), credentialItem.getPassword()));

			httpGet = new HttpGet(uri);
			httpContext = new BasicHttpContext();
			httpResponse = httpClient.execute(httpGet, httpContext);
			if (httpResponse != null) {
				statusLine = httpResponse.getStatusLine();
				httpEntity = httpResponse.getEntity();
			}
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

	private URI getRedirectLocation() {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			if (httpContext == null)
				return null;
			try {
				if (!redirectStrategy.isRedirected(httpGet, httpResponse,
						httpContext))
					return null;
				HttpUriRequest httpUri = redirectStrategy.getRedirect(httpGet,
						httpResponse, httpContext);
				if (httpUri == null)
					return null;
				return httpUri.getURI();
			} catch (ProtocolException e) {
				Logging.error(e);
				return null;
			}
		}
	}

	private Long getContentLength() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			return httpEntity.getContentLength();
		}
	}

	private String getContentDispositionFilename() {
		if (httpResponse == null)
			return null;
		Header header = httpResponse.getFirstHeader("Content-Disposition");
		if (header == null)
			return null;
		String s = header.getValue();
		int i1 = s.indexOf("filename=");
		if (i1 == -1)
			return null;
		i1 += 9;
		int i2 = s.indexOf(";", i1);
		String f = (i2 == -1) ? s.substring(i1) : s.substring(i1, i2);
		return f.replace("\"", "");
	}

	private String getContentBaseType() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			Header header = httpEntity.getContentType();
			if (header == null)
				return null;
			String v = header.getValue();
			int i = v.indexOf(';');
			if (i == -1)
				return v;
			return v.substring(0, i);
		}
	}

	private String getContentTypeCharset() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			return EntityUtils.getContentCharSet(httpEntity);
		}
	}

	private String getContentEncoding() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			Header header = httpEntity.getContentEncoding();
			if (header == null)
				return null;
			return header.getValue();
		}

	}

	private InputStream getContent() throws IllegalStateException, IOException {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			return httpEntity.getContent();
		}
	}

	private Integer getStatusCode() {
		synchronized (this) {
			if (statusLine == null)
				return null;
			return statusLine.getStatusCode();
		}
	}
}
