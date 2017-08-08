/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.remote.UriRead;
import com.jaeksoft.searchlib.remote.UriWriteObject;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.web.ServletTransaction.Method;
import freemarker.template.TemplateException;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpException;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

public abstract class AbstractServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 7013544620359275684L;

	protected final static String XML_CALL_KEY_STATUS = "Status";

	protected final static String XML_CALL_KEY_EXCEPTION = "Exception";

	protected final static String XML_CALL_KEY_MESSAGE = "Message";

	protected final static String XML_CALL_KEY_TRACE = "Trace";

	protected final static String XML_CALL_KEY_STATUS_ERROR = "Error";

	protected final static String XML_CALL_KEY_STATUS_OK = "OK";

	protected abstract void doRequest(ServletTransaction transaction) throws ServletException, TemplateException;

	public String serverURL;

	public String serverBaseURL;

	final private void doRequest(HttpServletRequest request, Method method, HttpServletResponse response) {

		ServletTransaction transaction = new ServletTransaction(this, request, method, response);

		try {
			ClientFactory.INSTANCE.properties.checkApi();
			buildUrls(request);
			doRequest(transaction);
			transaction.writeXmlResponse();
		} catch (TemplateException templateException) {
			try {
				transaction.getWriter("UTF-8").println(
						"<pre><code>" + templateException.getMessage() + "</pre></code>");
			} catch (IOException ioException) {
				Logging.error(templateException.getMessage(), templateException);
				Logging.warn(ioException.getMessage(), ioException);
			}
		} catch (Exception e1) {
			Logging.error(e1);
			transaction.addXmlResponse(XML_CALL_KEY_STATUS, XML_CALL_KEY_STATUS_ERROR);
			transaction.addXmlResponse(XML_CALL_KEY_MESSAGE, StringEscapeUtils.escapeXml11(e1.getMessage()));
			transaction.addXmlResponse(XML_CALL_KEY_EXCEPTION, e1.getClass().getName());
			try (final StringWriter sw = new StringWriter()) {
				try (final PrintWriter pw = new PrintWriter(sw)) {
					e1.printStackTrace(pw);
					Throwable t = e1;
					while ((t = t.getCause()) != null) {
						pw.println("Caused by...");
						t.printStackTrace(pw);
					}
					transaction.addXmlResponse(XML_CALL_KEY_TRACE, sw.toString());
					try {
						transaction.writeXmlResponse();
					} catch (Exception e2) {
						try {
							Logging.error(e2.getMessage(), e2);
							response.sendError(500, e1.getMessage());
						} catch (IOException ioException) {
							Logging.warn(ioException.getMessage(), ioException);
						}
					}
				}
			} catch (IOException ioException) {
				Logging.warn(ioException.getMessage(), ioException);
			}
		}
	}

	private void buildUrls(HttpServletRequest request) throws MalformedURLException {
		serverBaseURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
				request.getContextPath()).toString();
		StringBuilder sbUrl = new StringBuilder(request.getRequestURI());
		String qs = request.getQueryString();
		if (qs != null) {
			sbUrl.append('?');
			sbUrl.append(qs);
		}
		URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), sbUrl.toString());
		serverURL = url.toString();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doRequest(request, Method.POST, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doRequest(request, Method.GET, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) {
		doRequest(request, Method.PUT, response);
	}

	protected static URI buildUri(URI uri, String additionalPath, String indexName, String login, String apiKey,
			String additionnalQuery) throws URISyntaxException {
		StringBuilder path = new StringBuilder(uri.getPath());
		if (additionalPath != null)
			path.append(additionalPath);
		StringBuilder query = new StringBuilder();
		if (indexName != null) {
			query.append("index=");
			query.append(indexName);
		}
		if (login != null) {
			query.append("&login=");
			query.append(login);
		}
		if (apiKey != null) {
			query.append("&key=");
			query.append(apiKey);
		}
		if (additionnalQuery != null) {
			if (query.length() > 0)
				query.append("&");
			query.append(additionnalQuery);
		}
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path.toString(),
				query.toString(), uri.getFragment());

	}

	public static String getCallKeyValue(XPathParser xpp, String key) throws SearchLibException {
		try {
			return xpp.getNodeString("/response/entry[@key='" + key + "']");
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

	public static void checkCallError(XPathParser xpp) throws SearchLibException {
		if (!"Error" .equals(getCallKeyValue(xpp, XML_CALL_KEY_STATUS)))
			return;
		throw new SearchLibException(getCallKeyValue(xpp, XML_CALL_KEY_EXCEPTION));
	}

	public static void checkCallStatusOK(XPathParser xpp) throws SearchLibException {
		if ("OK" .equals(getCallKeyValue(xpp, XML_CALL_KEY_STATUS)))
			return;
		throw new SearchLibException("The returned status is not OK");
	}

	public static void checkCallKey(XPathParser xpp, String key, String value) throws SearchLibException {
		if (value.equals(getCallKeyValue(xpp, key)))
			return;
		throw new SearchLibException("The returned value does not match " + value);
	}

	protected static XPathParser call(final int secTimeOut, URI uri) throws SearchLibException {
		UriRead uriRead = null;
		try {
			uriRead = new UriRead(secTimeOut, uri);
			if (uriRead.getResponseCode() != 200)
				throw new IOException(
						uri + " returns " + uriRead.getResponseMessage() + "(" + uriRead.getResponseCode() + ")");
			return uriRead.getXmlContent();
		} catch (HttpException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} finally {
			if (uriRead != null)
				uriRead.close();
		}
	}

	protected static String sendObject(int secTimeOut, URI uri, Externalizable object) throws IOException {
		UriWriteObject writeObject = null;
		try {
			writeObject = new UriWriteObject(secTimeOut, uri, object);
			if (writeObject.getResponseCode() != 200)
				throw new IOException(writeObject.getResponseCode() + " " + writeObject.getResponseMessage() + ")");
			return writeObject.getResponseMessage();
		} finally {
			if (writeObject != null)
				writeObject.close();
		}
	}

	protected static Externalizable sendReceiveObject(int secTimeOut, URI uri, Externalizable object)
			throws IOException, ClassNotFoundException {
		UriWriteObject uwo = null;
		try {
			uwo = new UriWriteObject(secTimeOut, uri, object);
			if (uwo.getResponseCode() != 200)
				throw new IOException(uwo.getResponseMessage());
			return uwo.getResponseObject();
		} finally {
			if (uwo != null)
				uwo.close();
		}
	}

	public final static StringBuilder getApiUrl(StringBuilder sb, String servletPathName, Client client, User user)
			throws UnsupportedEncodingException {
		sb.append(servletPathName);
		sb.append("?use=");
		sb.append(URLEncoder.encode(client.getIndexName(), "UTF-8"));
		if (user != null)
			user.appendApiCallParameters(sb);
		return sb;
	}
}
