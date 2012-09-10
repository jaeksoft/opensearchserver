/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.remote.UriRead;
import com.jaeksoft.searchlib.remote.UriWriteObject;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.web.ServletTransaction.Method;

public abstract class AbstractServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7013544620359275684L;

	protected final static String XML_CALL_KEY_STATUS = "Status";

	protected final static String XML_CALL_KEY_EXCEPTION = "Exception";

	protected final static String XML_CALL_KEY_TRACE = "Trace";

	protected final static String XML_CALL_KEY_STATUS_ERROR = "Error";

	protected final static String XML_CALL_KEY_STATUS_OK = "OK";

	protected abstract void doRequest(ServletTransaction transaction)
			throws ServletException;

	public String serverURL;

	final private void doRequest(HttpServletRequest request, Method method,
			HttpServletResponse response) {

		ServletTransaction transaction = new ServletTransaction(this, request,
				method, response);

		try {
			ClientFactory.INSTANCE.properties.checkApi();
			serverURL = getCurrentServerURL(request);
			doRequest(transaction);
		} catch (Exception e) {
			transaction.addXmlResponse(XML_CALL_KEY_STATUS,
					XML_CALL_KEY_STATUS_ERROR);
			transaction.addXmlResponse(XML_CALL_KEY_EXCEPTION, e.toString());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Throwable t = e;
			while ((t = t.getCause()) != null) {
				pw.println("Caused by...");
				t.printStackTrace(pw);
			}
			transaction.addXmlResponse(XML_CALL_KEY_TRACE, sw.toString());
			pw.close();
			Logging.error(e);
		} finally {
			try {
				transaction.writeXmlResponse();
			} catch (Exception e) {
				try {
					Logging.error(e.getMessage(), e);
					response.sendError(500, e.getMessage());
				} catch (IOException e1) {
					Logging.warn(e1.getMessage(), e1);
				}
			}
		}
	}

	public String getCurrentServerURL(HttpServletRequest request)
			throws MalformedURLException {
		String file = request.getRequestURI();
		if (request.getQueryString() != null) {
			file += '?' + request.getQueryString();
		}
		URL url = new URL(request.getScheme(), request.getServerName(),
				request.getServerPort(), file);
		return url.toString();
	}

	public String getServerURL() {
		return serverURL;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.POST, response);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.GET, response);
	}

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.PUT, response);
	}

	protected static URI buildUri(URI uri, String additionalPath,
			String indexName, String login, String apiKey,
			String additionnalQuery) throws URISyntaxException {
		StringBuffer path = new StringBuffer(uri.getPath());
		if (additionalPath != null)
			path.append(additionalPath);
		StringBuffer query = new StringBuffer();
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
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
				uri.getPort(), path.toString(), query.toString(),
				uri.getFragment());

	}

	private static String getCallKeyValue(XPathParser xpp, String key)
			throws SearchLibException {
		try {
			return xpp.getNodeString("/response/entry[@key='" + key + "']");
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

	public static void checkCallError(XPathParser xpp)
			throws SearchLibException {
		if (!"Error".equals(getCallKeyValue(xpp, XML_CALL_KEY_STATUS)))
			return;
		throw new SearchLibException(getCallKeyValue(xpp,
				XML_CALL_KEY_EXCEPTION));
	}

	public static void checkCallStatusOK(XPathParser xpp)
			throws SearchLibException {
		if ("OK".equals(getCallKeyValue(xpp, XML_CALL_KEY_STATUS)))
			return;
		throw new SearchLibException("The returned status is not OK");
	}

	public static void checkCallKey(XPathParser xpp, String key, String value)
			throws SearchLibException {
		if (value.equals(getCallKeyValue(xpp, key)))
			return;
		throw new SearchLibException("The returned value does not match "
				+ value);
	}

	protected static XPathParser call(URI uri) throws SearchLibException {
		UriRead uriRead = null;
		try {
			uriRead = new UriRead(uri);
			if (uriRead.getResponseCode() != 200)
				throw new IOException(uri + " returns "
						+ uriRead.getResponseMessage() + "("
						+ uriRead.getResponseCode() + ")");
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

	protected static String sendObject(URI uri, Externalizable object)
			throws IOException {
		UriWriteObject writeObject = null;
		try {
			writeObject = new UriWriteObject(uri, object);
			if (writeObject.getResponseCode() != 200)
				throw new IOException(writeObject.getResponseCode() + " "
						+ writeObject.getResponseMessage() + ")");
			return writeObject.getResponseMessage();
		} finally {
			if (writeObject != null)
				writeObject.close();
		}
	}

	protected static Externalizable sendReceiveObject(URI uri,
			Externalizable object) throws IOException, ClassNotFoundException {
		UriWriteObject uwo = null;
		try {
			uwo = new UriWriteObject(uri, object);
			if (uwo.getResponseCode() != 200)
				throw new IOException(uwo.getResponseMessage());
			return uwo.getResponseObject();
		} finally {
			if (uwo != null)
				uwo.close();
		}
	}

	public final static StringBuffer getApiUrl(StringBuffer sb,
			String servletPathName, Client client, User user)
			throws UnsupportedEncodingException {
		sb.append(servletPathName);
		sb.append("?use=");
		sb.append(URLEncoder.encode(client.getIndexName(), "UTF-8"));
		if (user != null)
			user.appendApiCallParameters(sb);
		return sb;
	}
}
