/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
		out = null;
		in = null;
	}

	public Method getMethod() {
		return method;
	}

	public HttpServletRequest getServletRequest() {
		return this.request;
	}

	public HttpServletResponse getServletResponse() {
		return this.response;
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

	public void writeXmlResponse() throws IOException,
			TransformerConfigurationException, SAXException {
		if (xmlResponse == null)
			return;
		if (xmlResponse.size() == 0)
			return;
		response.setContentType("text/xml");
		XmlWriter xmlWriter = new XmlWriter(getWriter("UTF-8"), "UTF-8");

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

	public void sendFile(File file, String contentType)
			throws SearchLibException {
		response.setContentType(contentType);
		response.addHeader("Content-Disposition", "attachment; filename="
				+ file.getName());
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
}
