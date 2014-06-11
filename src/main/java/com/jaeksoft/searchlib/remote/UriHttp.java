/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.remote;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.XPathParser;

public abstract class UriHttp {

	private CloseableHttpClient httpClient = null;
	protected CloseableHttpResponse httpResponse = null;
	protected HttpEntity httpEntity = null;
	protected final RequestConfig requestConfig;

	protected UriHttp() {
		final int timeOut = 30;
		requestConfig = RequestConfig.custom().setSocketTimeout(timeOut * 1000)
				.setConnectTimeout(timeOut * 1000).build();
		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig).build();
	}

	protected void execute(HttpUriRequest request)
			throws ClientProtocolException, IOException {
		httpResponse = httpClient.execute(request);
		if (httpResponse != null)
			httpEntity = httpResponse.getEntity();
	}

	public int getResponseCode() throws IOException {
		return httpResponse.getStatusLine().getStatusCode();
	}

	public String getResponseMessage() throws IOException {
		return httpResponse.getStatusLine().getReasonPhrase();
	}

	public XPathParser getXmlContent() throws IllegalStateException,
			SAXException, IOException, ParserConfigurationException,
			SearchLibException {
		if (httpEntity == null)
			throw new SearchLibException("No content");
		Header header = httpEntity.getContentType();
		if (header == null)
			throw new SearchLibException("No content type");
		if (!header.getValue().startsWith("text/xml"))
			throw new SearchLibException("No XML content");
		return new XPathParser(httpEntity.getContent());
	}

	public void close() {
		try {
			if (httpEntity != null) {
				EntityUtils.consume(httpEntity);
				httpEntity = null;
			}
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
			httpEntity = null;
		}
		if (httpResponse != null) {
			IOUtils.closeQuietly(httpResponse);
			httpResponse = null;
		}
		if (httpClient != null) {
			IOUtils.closeQuietly(httpClient);
			httpClient = null;
		}
	}
}
