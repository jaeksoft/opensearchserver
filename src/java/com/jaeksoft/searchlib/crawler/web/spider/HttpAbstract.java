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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.net.ssl.SSLException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;

public abstract class HttpAbstract {

	private DefaultHttpClient httpClient = null;
	private RedirectStrategy redirectStrategy;
	private ProxyHandler proxyHandler;
	private HttpResponse httpResponse = null;
	private HttpContext httpContext = null;
	private HttpUriRequest httpUriRequest = null;
	private HttpEntity httpEntity = null;
	private StatusLine statusLine = null;

	public HttpAbstract(String userAgent, boolean bFollowRedirect,
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
		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				if (executionCount >= 1)
					return false;
				if (exception instanceof InterruptedIOException)
					return false; // TimeOut
				if (exception instanceof UnknownHostException)
					return false;// Unknown host
				if (exception instanceof ConnectException)
					return false;// Connection refused
				if (exception instanceof SSLException)
					return false;// SSL handshake exception
				HttpRequest request = (HttpRequest) context
						.getAttribute(ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent)
					return true; // Retry if the request is considered
									// idempotent
				return false;
			}
		};
		httpClient.setHttpRequestRetryHandler(myRetryHandler);
	}

	protected void reset() {
		httpResponse = null;
		httpUriRequest = null;
		synchronized (this) {
			if (httpEntity != null) {
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					Logging.warn(e.getMessage(), e);
				}
				httpEntity = null;
			}
			httpEntity = null;
			statusLine = null;
		}
	}

	protected void execute(HttpUriRequest httpUriRequest,
			CredentialItem credentialItem) throws ClientProtocolException,
			IOException {
		this.httpUriRequest = httpUriRequest;
		URI uri = httpUriRequest.getURI();
		if (proxyHandler != null)
			proxyHandler.check(httpClient, uri);
		CredentialsProvider credentialProvider = httpClient
				.getCredentialsProvider();
		if (credentialItem == null)
			credentialProvider.clear();
		else
			credentialItem.setUpCredentials(httpClient.getParams(),
					credentialProvider);
		httpContext = new BasicHttpContext();
		httpResponse = httpClient.execute(httpUriRequest, httpContext);
		if (httpResponse == null)
			return;
		statusLine = httpResponse.getStatusLine();
		httpEntity = httpResponse.getEntity();
	}

	public URI getRedirectLocation() {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			if (httpContext == null)
				return null;
			try {
				if (!redirectStrategy.isRedirected(httpUriRequest,
						httpResponse, httpContext))
					return null;
				HttpUriRequest httpUri = redirectStrategy.getRedirect(
						httpUriRequest, httpResponse, httpContext);
				if (httpUri == null)
					return null;
				return httpUri.getURI();
			} catch (ProtocolException e) {
				Logging.error(e);
				return null;
			}
		}
	}

	public Long getContentLength() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			return httpEntity.getContentLength();
		}
	}

	public String getContentDispositionFilename() {
		synchronized (this) {
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
	}

	public String getContentBaseType() {
		synchronized (this) {
			Header header = null;
			if (httpEntity != null)
				header = httpEntity.getContentType();
			if (header == null)
				header = httpResponse.getFirstHeader("Content-Type");
			if (header == null)
				return null;
			String v = header.getValue();
			int i = v.indexOf(';');
			if (i == -1)
				return v;
			return v.substring(0, i);
		}
	}

	public String getContentTypeCharset() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			ContentType ct = ContentType.getOrDefault(httpEntity);
			if (ct == null)
				return null;
			Charset charset = ct.getCharset();
			if (charset == null)
				return null;
			return charset.name();
		}
	}

	public String getContentEncoding() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			Header header = httpEntity.getContentEncoding();
			if (header == null)
				return null;
			return header.getValue();
		}

	}

	protected InputStream getContent() throws IllegalStateException,
			IOException {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			return httpEntity.getContent();
		}
	}

	public Integer getStatusCode() {
		synchronized (this) {
			if (statusLine == null)
				return null;
			return statusLine.getStatusCode();
		}
	}

	public void release() {
		synchronized (this) {
			try {
				reset();
				if (httpClient != null)
					httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				Logging.warn(e.getMessage(), e);
			}
		}
	}

	public Header[] getHeaders() {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			return httpResponse.getAllHeaders();
		}
	}

}
