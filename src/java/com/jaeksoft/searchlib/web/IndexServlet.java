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

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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

	private void updateDoc(Client client, String indexName, IndexDocument doc)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (indexName == null)
			client.updateDocument(doc);
		else
			client.updateDocument(indexName, doc);
	}

	private void updateDoc(Client client, String indexName,
			Collection<IndexDocument> indexDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (indexName == null)
			client.updateDocuments(indexDocuments);
		else
			client.updateDocuments(indexName, indexDocuments);
	}

	private void updateDoc(Client client, String indexName,
			IndexRequest indexRequest) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		updateDoc(client, indexName, indexRequest.getCollection());
	}

	private void doObjectRequest(Client client, HttpServletRequest request,
			String indexName) throws ServletException {
		StreamReadObject readObject = null;
		try {
			readObject = new StreamReadObject(request.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof IndexRequest)
				updateDoc(client, indexName, (IndexRequest) obj);
			else if (obj instanceof IndexDocument)
				updateDoc(client, indexName, (IndexDocument) obj);
		} catch (Exception e) {
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
			if (ct != null && ct.toLowerCase().contains("xml"))
				client.updateXmlDocuments(indexName, request.getInputStream());
			else
				doObjectRequest(client, request, indexName);
			PrintWriter writer = transaction.getWriter("UTF-8");
			writer.println("OK");
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
		}
	}

	public static void update(URI uri, String indexName, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		sendObject(buildUri(uri, "/index", indexName, null), document);
	}

	public static void update(URI uri, String indexName,
			Collection<IndexDocument> indexDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		sendObject(buildUri(uri, "/index", indexName, null), new IndexRequest(
				indexDocuments));
	}
}
