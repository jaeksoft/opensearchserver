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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.http.WebManager;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ServletTransaction {

	public enum Method {
		PUT, POST, GET, HEAD;
	}

	private User loggedUser;

	private Client client;

	private String indexName;

	private AbstractServlet servlet;

	private HttpServletResponse response;

	private HttpServletRequest request;

	private PrintWriter writer;

	private BufferedReader reader;

	private ServletOutputStream out;

	private ServletInputStream in;

	private Method method;

	private XmlWriter xmlWriter;

	private Map<String, String> xmlResponse;

	public ServletTransaction(AbstractServlet servlet,
			HttpServletRequest request, Method method,
			HttpServletResponse response) {
		this.method = method;
		this.servlet = servlet;
		this.response = response;
		this.request = request;
		indexName = null;
		xmlResponse = null;
		writer = null;
		reader = null;
		xmlWriter = null;
		out = null;
		in = null;
	}

	public Method getMethod() {
		return method;
	}

	public User getLoggedUser() throws SearchLibException, InterruptedException {
		if (loggedUser != null)
			return loggedUser;
		if (ClientCatalog.getUserList().isEmpty())
			return null;
		String login = request.getParameter("login");
		String key = request.getParameter("key");
		loggedUser = ClientCatalog.authenticateKey(login, key);
		if (loggedUser == null) {
			Thread.sleep(500);
			throw new SearchLibException("Bad credential");
		}
		return loggedUser;
	}

	public String getIndexName() {
		if (indexName != null)
			return indexName;
		indexName = request.getParameter("use");
		if (indexName == null)
			indexName = request.getParameter("index");
		return indexName;
	}

	public Client getClient() throws SearchLibException, NamingException {
		if (client != null)
			return client;
		client = ClientCatalog.getClient(getIndexName());
		return client;
	}

	public Client getClientApi(String use) throws SearchLibException,
			NamingException {
		if (client != null)
			return client;
		client = ClientCatalog.getClient(use);
		return client;
	}

	public void addXmlResponse(String key, String value) {
		if (xmlResponse == null)
			xmlResponse = new LinkedHashMap<String, String>();
		xmlResponse.put(key, value);
	}

	public BufferedReader getReader() throws IOException {
		if (in != null)
			throw new IOException("InputStream delivered before");
		if (reader != null)
			return reader;
		reader = request.getReader();
		return reader;
	}

	public ServletInputStream getInputStream() throws IOException {
		if (reader != null)
			throw new IOException("Reader delivered before");
		if (in != null)
			return in;
		in = request.getInputStream();
		return in;
	}

	public PrintWriter getWriter(String encoding) throws IOException {
		if (out != null)
			throw new IOException("OutputStream delivered before");
		if (writer != null)
			return writer;
		response.setCharacterEncoding(encoding);
		writer = response.getWriter();
		return writer;
	}

	public XmlWriter getXmlWriter(String encoding) throws SearchLibException {
		if (xmlWriter != null)
			return xmlWriter;
		try {
			xmlWriter = new XmlWriter(getWriter(encoding), encoding);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		return xmlWriter;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IOException("Writer delivered before");
		if (out != null)
			return out;
		out = response.getOutputStream();
		return out;
	}

	public void forward(String path) throws ServletException {
		RequestDispatcher dispatcher = servlet.getServletContext()
				.getRequestDispatcher(path);
		try {
			dispatcher.forward(request, response);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	public HashSet<String> getClassDetail(HttpServletRequest request) {
		HashSet<String> classDetail = new HashSet<String>();
		String[] values = request.getParameterValues("details");
		if (values != null)
			for (String value : values)
				classDetail.add(value);
		return classDetail;
	}

	public final String getResponseContentType() {
		return response.getContentType();
	}

	public final void setResponseContentType(String contentType) {
		response.setContentType(contentType);
	}

	public void writeXmlResponse() throws SearchLibException, SAXException {
		if (xmlResponse == null)
			return;
		if (xmlResponse.size() == 0)
			return;
		getXmlWriter("UTF-8");
		setResponseContentType("text/xml");

		xmlWriter.startElement("response");

		for (Map.Entry<String, String> entry : xmlResponse.entrySet()) {
			xmlWriter.startElement("entry", "key", entry.getKey());
			xmlWriter.textNode(entry.getValue());
			xmlWriter.endElement();
		}

		xmlWriter.endElement();
		xmlWriter.endDocument();
	}

	public WebApp getWebApp() {
		return WebManager.getWebApp(servlet.getServletContext());
	}

	public void sendFile(File file, String filename, String contentType)
			throws SearchLibException {
		response.setContentType(contentType);
		response.addHeader("Content-Disposition", "attachment; filename="
				+ filename);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			ServletOutputStream outputStream = getOutputStream();
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
		}
	}

	public final String getParameterString(String name) {
		return request.getParameter(name);
	}

	public final String getParameterString(String name, String defaultValue) {
		String p = request.getParameter(name);
		return p == null ? defaultValue : p;
	}

	public final Integer getParameterInteger(String name) {
		return getParameterInteger(name, null);
	}

	public final int getParameterLong(String name, int defaultValue) {
		return getParameterLong(name, new Integer(defaultValue));
	}

	public final Integer getParameterInteger(String name, Integer defaultValue) {
		String p = request.getParameter(name);
		if (p == null || p.length() == 0)
			return defaultValue;
		return Integer.parseInt(p);
	}

	public final Long getParameterLong(String name) {
		return getParameterLong(name, null);
	}

	public final long getParameterLong(String name, long defaultValue) {
		return getParameterLong(name, new Long(defaultValue));
	}

	public final Long getParameterLong(String name, Long defaultValue) {
		String p = request.getParameter(name);
		if (p == null || p.length() == 0)
			return defaultValue;
		return Long.parseLong(p);
	}

	public final Boolean getParameterBoolean(String name) {
		return getParameterBoolean(name, null);
	}

	public final boolean getParameterBoolean(String name, boolean defaultValue) {
		Boolean b = getParameterBoolean(name, null);
		if (b == null)
			return defaultValue;
		return b;
	}

	public final boolean getParameterBoolean(String name, String valueExpected,
			boolean defaultValue) {
		Boolean b = getParameterBoolean(name, valueExpected);
		if (b == null)
			return defaultValue;
		return b;
	}

	public final Boolean getParameterBoolean(String name, String valueExpected) {
		String p = request.getParameter(name);
		if (p == null)
			return null;
		if (valueExpected != null)
			return valueExpected.equalsIgnoreCase(p);
		return true;
	}

	public final String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}

	public String getRequestContentType() {
		return request.getContentType();
	}

	public void setRequestAttribute(String name, Object value) {
		request.setAttribute(name, value);
	}

	public void setRequestEncoding(String encoding)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding(encoding);
	}
}
