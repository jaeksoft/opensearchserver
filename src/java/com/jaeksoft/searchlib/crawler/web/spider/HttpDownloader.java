/**   
 * License Agreement for Jaeksoft WebSearch
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft WebSearch.
 *
 * Jaeksoft WebSearch is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft WebSearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft WebSearch. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpDownloader {

	private HttpClient httpClient = null;
	private GetMethod currentMethod = null;

	public HttpDownloader() {
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(60000);
		params.setSoTimeout(5000);
		params.setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(0, false));
		httpClient = new HttpClient(params);
	}

	public void release() {
		try {
			if (currentMethod != null) {
				currentMethod.releaseConnection();
				currentMethod = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GetMethod get(String url, String userAgent) throws IOException {
		synchronized (this) {
			if (currentMethod != null)
				throw new IOException("Another GET method is running");
			currentMethod = new GetMethod(url);
			if (userAgent != null)
				currentMethod.addRequestHeader("User-agent", userAgent);
			httpClient.executeMethod(currentMethod);
			return currentMethod;
		}
	}
}
