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

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.request.IndexRequest;

public class IndexServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3855116559376800406L;

	private int updateDoc(Client client, String indexName, IndexDocument doc)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (indexName == null)
			return client.updateDocument(doc) ? 1 : 0;
		else
			return client.updateDocument(indexName, doc) ? 1 : 0;
	}

	private int updateDoc(Client client, String indexName,
			Collection<IndexDocument> indexDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (indexName == null)
			return client.updateDocuments(indexDocuments);
		else
			return client.updateDocuments(indexName, indexDocuments);
	}

	private int updateDoc(Client client, String indexName,
			IndexRequest indexRequest) throws NoSuchAlgorithmException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return updateDoc(client, indexName, indexRequest.getCollection());
	}

	private int doObjectRequest(Client client, HttpServletRequest request,
			String indexName) throws ServletException {
		StreamReadObject readObject = null;
		try {
			readObject = new StreamReadObject(request.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof IndexRequest)
				return updateDoc(client, indexName, (IndexRequest) obj);
			else if (obj instanceof IndexDocument)
				return updateDoc(client, indexName, (IndexDocument) obj);
			throw new ServletException("Nothing to do");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		} finally {
			if (readObject != null)
				readObject.close();
		}
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			Client client = Client.getWebAppInstance();
			HttpServletRequest request = transaction.getServletRequest();
			String indexName = request.getParameter("index");
			String ct = request.getContentType();
			Object result = null;
			if (ct != null && ct.toLowerCase().contains("xml"))
				result = client.updateXmlDocuments(indexName, new InputSource(
						request.getInputStream()));
			else
				result = doObjectRequest(client, request, indexName);
			transaction.addXmlResponse("Status", "OK");
			transaction.addXmlResponse("Count", result.toString());
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (XPathExpressionException e) {
			throw new ServletException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}
	}

	public static boolean update(URI uri, String indexName,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		String msg = sendObject(buildUri(uri, "/index", indexName, null),
				document);
		return Boolean.parseBoolean(msg.trim());
	}

	public static int update(URI uri, String indexName,
			Collection<IndexDocument> indexDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		String msg = sendObject(buildUri(uri, "/index", indexName, null),
				new IndexRequest(indexDocuments));
		if (msg == null)
			return 0;
		return Integer.parseInt(msg.trim());
	}
}
