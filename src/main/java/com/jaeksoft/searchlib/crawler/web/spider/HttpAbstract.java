/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2013-2016 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.cifs.NTLMSchemeFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.*;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class HttpAbstract {

	private final int msTimeOut;
	private final boolean followRedirect;
	private final CloseableHttpClient httpClient;
	private RedirectStrategy redirectStrategy;
	private HttpResponse httpResponse = null;
	private final HttpClientContext httpClientContext;
	private HttpRequestBase httpBaseRequest = null;
	private final ProxyHandler proxyHandler;
	private final HttpHost proxyHost;
	private HttpEntity httpEntity = null;
	private StatusLine statusLine = null;
	private CredentialsProvider credentialsProvider;
	private final CookieStore cookieStore;

	public HttpAbstract(String userAgent, boolean bFollowRedirect, ProxyHandler proxyHandler, int msTimeOut)
			throws IOException {

		this.followRedirect = bFollowRedirect;
		this.msTimeOut = msTimeOut;
		HttpClientBuilder builder = HttpClients.custom();

		// Timeout
		builder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(msTimeOut).build());
		builder.setConnectionTimeToLive(msTimeOut * 2, TimeUnit.MILLISECONDS);

		SSLContext sslContext;
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
			}).build();
			builder.setSSLContext(sslContext);
		} catch (KeyManagementException e) {
			throw new IOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		} catch (KeyStoreException e) {
			throw new IOException(e);
		}

		HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
		builder.setSSLSocketFactory(sslSocketFactory);

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
		proxyHost = proxyHandler == null ? null : proxyHandler.getAnyProxy();

		Registry<AuthSchemeProvider> authSchemeRegistry =
				RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.NTLM, new NTLMSchemeFactory())
						.register(AuthSchemes.BASIC, new BasicSchemeFactory())
						.register(AuthSchemes.DIGEST, new DigestSchemeFactory())
						.register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
						.register(AuthSchemes.KERBEROS, new KerberosSchemeFactory()).build();

		credentialsProvider = new BasicCredentialsProvider();
		builder.setDefaultCredentialsProvider(credentialsProvider);
		builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);

		httpClient = builder.build();

		httpClientContext = HttpClientContext.create();
		cookieStore = new BasicCookieStore();
		httpClientContext.setCookieStore(cookieStore);

	}

	protected void reset() {
		httpResponse = null;
		httpBaseRequest = null;
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

	protected void execute(HttpRequestBase httpBaseRequest, CredentialItem credentialItem, List<CookieItem> cookies)
			throws IOException, URISyntaxException {

		// Filling the cookie store with configuration cookies
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
		// Cookies uses best match policy
		RequestConfig.Builder configBuilder =
				RequestConfig.custom().setSocketTimeout(msTimeOut).setConnectionRequestTimeout(msTimeOut)
						.setConnectTimeout(msTimeOut).setCookieSpec(CookieSpecs.STANDARD)
						.setRedirectsEnabled(followRedirect);

		if (credentialItem == null)
			credentialsProvider.clear();
		else
			credentialItem.setUpCredentials(credentialsProvider, httpBaseRequest);

		URI uri = httpBaseRequest.getURI();
		if (proxyHandler != null && proxyHost != null)
			if (proxyHandler.isProxy(uri))
				proxyHandler.applyProxy(configBuilder, proxyHost, credentialsProvider);

		httpBaseRequest.setConfig(configBuilder.build());

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
			try {
				if (!redirectStrategy.isRedirected(httpBaseRequest, httpResponse, httpClientContext)) {
					Object redirects = httpClientContext.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
					if (redirects == null)
						return null;
					if (redirects instanceof List<?>) {
						List<?> redirectCollection = (List<?>) redirects;
						if (CollectionUtils.isEmpty(redirectCollection))
							return null;
						redirects = redirectCollection.get(redirectCollection.size() - 1);
					}
					if (redirects instanceof URI)
						return ((URI) redirects);
					else
						return new URI(redirects.toString());
				}
				HttpUriRequest httpUri = redirectStrategy.getRedirect(httpBaseRequest, httpResponse, httpClientContext);
				if (httpUri == null)
					return null;
				return httpUri.getURI();
			} catch (ProtocolException e) {
				Logging.info(e);
				return null;
			} catch (URISyntaxException e) {
				Logging.info(e);
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

	private final static String[] LastModifiedDateFormats =
			{ "EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss z", "EEEE, dd-MMM-yy HH:mm:ss z",
					"EEE MMM d HH:mm:ss yyyy" };

	private final static ThreadSafeDateFormat[] httpDatesFormats;

	static {
		int i = 0;
		httpDatesFormats = new ThreadSafeDateFormat[LastModifiedDateFormats.length * 2];
		for (String format : LastModifiedDateFormats) {
			httpDatesFormats[i++] = new ThreadSafeSimpleDateFormat(format, Locale.ENGLISH);
			httpDatesFormats[i++] = new ThreadSafeSimpleDateFormat(format);
		}
	}

	;

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

	public static void main(String[] argv) throws IOException {
		for (ThreadSafeDateFormat dateFormat : httpDatesFormats) {
			try {
				System.out.println(dateFormat.parse("Thu, 21 Feb 2013 20:11:52 GMT").getTime());
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

	protected InputStream getContent() throws IllegalStateException, IOException {
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
			reset();
			if (httpClient != null)
				IOUtils.close(httpClient);
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
