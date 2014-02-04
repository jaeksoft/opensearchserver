/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.IOUtils;

public abstract class HttpAbstract {

	private CloseableHttpClient httpClient = null;
	private RedirectStrategy redirectStrategy;
	private HttpResponse httpResponse = null;
	private HttpClientContext httpClientContext = null;
	private HttpRequestBase httpBaseRequest = null;
	private ProxyHandler proxyHandler;
	private HttpEntity httpEntity = null;
	private StatusLine statusLine = null;
	private BasicCookieStore cookieStore;
	private CredentialsProvider credentialsProvider;

	public HttpAbstract(String userAgent, boolean bFollowRedirect,
			ProxyHandler proxyHandler) {
		HttpClientBuilder builder = HttpClients.custom();

		redirectStrategy = new DefaultRedirectStrategy();

		if (userAgent != null) {
			userAgent = userAgent.trim();
			if (userAgent.length() > 0)
				builder.setUserAgent(userAgent);
			else
				userAgent = null;
		}
		if (!bFollowRedirect)
			builder.disableRedirectHandling();

		this.proxyHandler = proxyHandler;

		credentialsProvider = new BasicCredentialsProvider();
		builder.setDefaultCredentialsProvider(credentialsProvider);

		cookieStore = new BasicCookieStore();
		builder.setDefaultCookieStore(cookieStore);

		builder.setDefaultCredentialsProvider(credentialsProvider);

		httpClient = builder.build();

	}

	protected void reset() {
		httpResponse = null;
		httpBaseRequest = null;
		httpClientContext = null;
		synchronized (this) {
			if (httpEntity != null) {
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					Logging.warn(e.getMessage(), e);
				}
				httpEntity = null;
			}
			statusLine = null;
		}
	}

	protected void execute(HttpRequestBase httpBaseRequest,
			CredentialItem credentialItem, List<CookieItem> cookies)
			throws ClientProtocolException, IOException, URISyntaxException {

		if (!CollectionUtils.isEmpty(cookies)) {
			List<Cookie> cookieList = cookieStore.getCookies();
			for (CookieItem cookie : cookies) {
				Cookie newCookie = cookie.getCookie();
				if (!cookieList.contains(newCookie))
					cookieStore.addCookie(newCookie);
			}
		}

		this.httpBaseRequest = httpBaseRequest;

		// No more than one 1 minute to establish the connection
		// No more than 10 minutes to establish the socket
		// Enable stales connection checking
		// Cookies uses browser compatibility
		RequestConfig.Builder configBuilber = RequestConfig.custom()
				.setSocketTimeout(1000 * 60 * 10).setConnectTimeout(1000 * 60)
				.setStaleConnectionCheckEnabled(true)
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);

		URI uri = httpBaseRequest.getURI();
		if (proxyHandler != null)
			proxyHandler.check(configBuilber, uri);

		httpBaseRequest.setConfig(configBuilber.build());

		if (credentialItem == null)
			credentialsProvider.clear();
		else
			credentialItem.setUpCredentials(credentialsProvider);

		httpClientContext = new HttpClientContext();

		httpResponse = httpClient.execute(httpBaseRequest, httpClientContext);
		if (httpResponse == null)
			return;
		statusLine = httpResponse.getStatusLine();
		httpEntity = httpResponse.getEntity();
	}

	public URI getRedirectLocation() {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			if (httpClientContext == null)
				return null;
			try {
				if (!redirectStrategy.isRedirected(httpBaseRequest,
						httpResponse, httpClientContext))
					return null;
				HttpUriRequest httpUri = redirectStrategy.getRedirect(
						httpBaseRequest, httpResponse, httpClientContext);
				if (httpUri == null)
					return null;
				return httpUri.getURI();
			} catch (ProtocolException e) {
				Logging.error(e);
				return null;
			}
		}
	}

	final public Long getContentLength() {
		synchronized (this) {
			if (httpEntity != null)
				return httpEntity.getContentLength();
			Header header = httpResponse.getFirstHeader("Content-Length");
			if (header == null)
				return null;
			String value = header.getValue();
			if (value == null)
				return null;
			return new Long(value);
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

	// Sun, 06 Nov 1994 08:49:37 GMT ; RFC 822, updated by RFC 1123
	// Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
	// Sun Nov 6 08:49:37 1994

	private final static String[] LastModifiedDateFormats = {
			"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss z",
			"EEEE, dd-MMM-yy HH:mm:ss z", "EEE MMM d HH:mm:ss yyyy" };

	private final static ThreadSafeDateFormat[] httpDatesFormats;

	static {
		int i = 0;
		httpDatesFormats = new ThreadSafeDateFormat[LastModifiedDateFormats.length * 2];
		for (String format : LastModifiedDateFormats) {
			httpDatesFormats[i++] = new ThreadSafeSimpleDateFormat(format,
					Locale.ENGLISH);
			httpDatesFormats[i++] = new ThreadSafeSimpleDateFormat(format);
		}
	};

	public Long getLastModified() {
		synchronized (this) {
			Header header = httpResponse.getFirstHeader("Last-Modified");
			if (header == null)
				return null;
			String v = header.getValue();
			if (v == null)
				return null;
			ParseException parseException = null;
			for (ThreadSafeDateFormat dateFormat : httpDatesFormats) {
				try {
					return dateFormat.parse(v).getTime();
				} catch (ParseException e) {
					parseException = e;
				}
			}
			if (parseException != null)
				Logging.warn(parseException);
			return null;
		}
	}

	public final static void main(String[] argv) {
		for (ThreadSafeDateFormat dateFormat : httpDatesFormats) {
			try {
				System.out.println(dateFormat.parse(
						"Thu, 21 Feb 2013 20:11:52 GMT").getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public String getContentTypeCharset() {
		synchronized (this) {
			if (httpEntity == null)
				return null;
			try {
				ContentType ct = ContentType.getOrDefault(httpEntity);
				if (ct == null)
					return null;
				Charset charset = ct.getCharset();
				if (charset == null)
					return null;
				return charset.name();
			} catch (UnsupportedCharsetException e) {
				Logging.warn(e);
				return null;
			}
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

	public String getContentLocation() {
		synchronized (this) {
			if (httpResponse == null)
				return null;
			Header header = httpResponse.getFirstHeader("Content-Location");
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

	public String getReasonPhrase() {
		synchronized (this) {
			if (statusLine == null)
				return null;
			return statusLine.getReasonPhrase();
		}
	}

	public void release() {
		synchronized (this) {
			try {
				reset();
				IOUtils.close(httpClient);
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
