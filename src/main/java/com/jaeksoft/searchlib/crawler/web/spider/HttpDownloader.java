/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.spider;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.HeaderItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class HttpDownloader extends HttpAbstract {

	public static enum Method {
		GET(false), POST(true), PUT(false), DELETE(false), HEAD(false), PATCH(true), OPTIONS(false);

		private final boolean acceptPayload;

		private Method(boolean payload) {
			this.acceptPayload = payload;
		}

		/**
		 * @return the acceptPayload
		 */
		public boolean isAcceptPayload() {
			return acceptPayload;
		}

		public final static Method find(String text, Method defaultMethod) {
			for (Method method : values())
				if (method.name().equalsIgnoreCase(text))
					return method;
			return defaultMethod;
		}

	}

	public HttpDownloader(final String userAgent, final boolean bFollowRedirect, final ProxyHandler proxyHandler,
			int msTimeOut) throws IOException {
		super(userAgent, bFollowRedirect, proxyHandler, msTimeOut);
	}

	private final void addHeader(HttpRequest httpRequest, List<HeaderItem> headers) {
		if (headers == null)
			return;
		for (HeaderItem header : headers)
			httpRequest.addHeader(header.getHeader());
	}

	private final DownloadItem getDownloadItem(final URI uri) throws IllegalStateException, IOException {
		final DownloadItem downloadItem = new DownloadItem(uri, System.currentTimeMillis(), false);
		downloadItem.setRedirectLocation(getRedirectLocation());
		downloadItem.setContentLength(getContentLength());
		downloadItem.setLastModified(getLastModified());
		downloadItem.setContentDispositionFilename(getContentDispositionFilename());
		downloadItem.setContentBaseType(getContentBaseType());
		downloadItem.setContentEncoding(getContentEncoding());
		downloadItem.setContentLocation(getContentLocation());
		downloadItem.setContentTypeCharset(getContentTypeCharset());
		downloadItem.setStatusCode(getStatusCode());
		downloadItem.setReasonPhrase(getReasonPhrase());
		downloadItem.setContentInputStream(getContent());
		downloadItem.setHeaders(getHeaders());
		return downloadItem;
	}

	private final DownloadItem request(final HttpRequestBase httpRequestBase, final CredentialItem credentialItem,
			final List<HeaderItem> headers, final List<CookieItem> cookies, final HttpEntity entity)
			throws ClientProtocolException, IOException, URISyntaxException, IllegalStateException {
		synchronized (this) {
			reset();
			if (entity != null)
				((HttpEntityEnclosingRequest) httpRequestBase).setEntity(entity);
			addHeader(httpRequestBase, headers);
			execute(httpRequestBase, credentialItem, cookies);
			return getDownloadItem(httpRequestBase.getURI());
		}
	}

	public final DownloadItem request(final URI uri, final Method method, final CredentialItem credentialItem,
			final List<HeaderItem> headers, final List<CookieItem> cookies, final HttpEntity entity)
			throws ClientProtocolException, IllegalStateException, IOException, URISyntaxException {
		HttpRequestBase httpRequestBase;
		switch (method) {
		case GET:
			httpRequestBase = new HttpGet(uri);
			break;
		case POST:
			httpRequestBase = new HttpPost(uri);
			break;
		case PUT:
			httpRequestBase = new HttpPut(uri);
			break;
		case DELETE:
			httpRequestBase = new HttpDelete(uri);
			break;
		case OPTIONS:
			httpRequestBase = new HttpOptions(uri);
			break;
		case PATCH:
			httpRequestBase = new HttpPatch(uri);
			break;
		case HEAD:
			httpRequestBase = new HttpHead(uri);
			break;
		default:
			throw new IOException("Unkown method: " + method);
		}
		return request(httpRequestBase, credentialItem, headers, cookies, entity);
	}

	public DownloadItem patch(URI uri, CredentialItem credentialItem, List<HeaderItem> headers,
			List<CookieItem> cookies, HttpEntity entity)
			throws ClientProtocolException, IOException, URISyntaxException, IllegalStateException, SearchLibException {
		return request(uri, Method.PATCH, credentialItem, headers, cookies, entity);
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem, List<HeaderItem> headers, List<CookieItem> cookies)
			throws IOException, IllegalStateException, URISyntaxException {
		return request(uri, Method.GET, credentialItem, headers, cookies, null);
	}

	public DownloadItem get(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException, IllegalStateException, URISyntaxException {
		return get(uri, credentialItem, null, null);
	}

	public DownloadItem head(URI uri, CredentialItem credentialItem, List<HeaderItem> headers, List<CookieItem> cookies)
			throws ClientProtocolException, IOException, IllegalStateException, SearchLibException, URISyntaxException {
		return request(uri, Method.HEAD, credentialItem, headers, cookies, null);
	}

	public DownloadItem head(URI uri, CredentialItem credentialItem)
			throws ClientProtocolException, IOException, IllegalStateException, SearchLibException, URISyntaxException {
		return head(uri, credentialItem, null, null);
	}

	public DownloadItem post(URI uri, CredentialItem credentialItem, List<HeaderItem> headers, List<CookieItem> cookies,
			HttpEntity entity) throws ClientProtocolException, IOException, IllegalStateException, URISyntaxException {
		return request(uri, Method.POST, credentialItem, headers, cookies, entity);
	}

	public DownloadItem options(URI uri, CredentialItem credentialItem, List<HeaderItem> headers,
			List<CookieItem> cookies)
			throws ClientProtocolException, IOException, IllegalStateException, SearchLibException, URISyntaxException {
		return request(uri, Method.OPTIONS, credentialItem, headers, cookies, null);
	}

	public DownloadItem delete(URI uri, CredentialItem credentialItem, List<HeaderItem> headers,
			List<CookieItem> cookies)
			throws ClientProtocolException, IOException, IllegalStateException, SearchLibException, URISyntaxException {
		return request(uri, Method.DELETE, credentialItem, headers, cookies, null);
	}

	public DownloadItem put(URI uri, CredentialItem credentialItem, List<HeaderItem> headers, List<CookieItem> cookies,
			HttpEntity entity)
			throws ClientProtocolException, IOException, IllegalStateException, SearchLibException, URISyntaxException {
		return request(uri, Method.PUT, credentialItem, headers, cookies, entity);
	}

	public static final String UA_CHROME =
			"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";

	public static final void main(String[] args) throws IOException {
		try {
			HttpDownloader downloader = new HttpDownloader("OpenSearchServerBot", true, null, 60000);
			downloader.get(new URI("http://www.opensearchserver.com/robots.txt"), null);
			downloader.get(new URI("http://www.opensearchserver.com/"), null);
			downloader.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
