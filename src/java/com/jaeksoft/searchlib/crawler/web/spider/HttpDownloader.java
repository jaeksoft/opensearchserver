/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class HttpDownloader {

	private HttpClient httpClient = null;
	private HttpGet httpGet = null;
	private HttpContext httpContext = null;
	private HttpResponse httpResponse = null;
	private RedirectHandler redirectHandler;

	public HttpDownloader(String userAgent) {
		redirectHandler = new DefaultRedirectHandler();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		paramsBean.setUserAgent(userAgent);
		HttpClientParams.setRedirecting(params, false);
		httpClient = new DefaultHttpClient(params);
		// TIMEOUT ?
		// RETRY HANDLER ?
	}

	public void release() {
		synchronized (this) {
			try {
				httpContext = null;
				httpGet = null;
				httpResponse = null;
				if (httpClient != null)
					httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void get(String url) throws IOException {
		synchronized (this) {
			httpGet = new HttpGet(url);
			httpContext = new BasicHttpContext();
			httpResponse = httpClient.execute(httpGet, httpContext);
		}
	}

	public HttpResponse getResponse() {
		synchronized (this) {
			return httpResponse;
		}
	}

	public URI getRedirectLocation() throws ProtocolException {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			if (httpContext == null)
				return null;
			if (!redirectHandler.isRedirectRequested(httpResponse, httpContext))
				return null;
			return redirectHandler.getLocationURI(httpResponse, httpContext);
		}
	}
}
