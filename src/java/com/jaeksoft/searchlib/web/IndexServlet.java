/**   
 * License Agreement for Jaeksoft WebSearch
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.util.XPathParser;

public class IndexServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3855116559376800406L;

	private void updateDoc(Client client, IndexDocument doc, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		client.getIndex().updateDocument(client.getSchema(), doc, forceLocal);
	}

	private void updateDoc(Client client,
			List<? extends IndexDocument> docList, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		client.getIndex().updateDocuments(null, client.getSchema(), docList,
				forceLocal);
	}

	private void updateDoc(Client client, String indexName, IndexDocument doc,
			boolean forceLocal) throws NoSuchAlgorithmException, IOException {
		if (indexName == null)
			updateDoc(client, doc, forceLocal);
		else
			client.getIndex().updateDocument(indexName, client.getSchema(),
					doc, forceLocal);
	}

	private void updateDoc(Client client, String indexName,
			List<? extends IndexDocument> docList, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (indexName == null)
			updateDoc(client, docList, forceLocal);
		client.getIndex().updateDocuments(indexName, client.getSchema(),
				docList, forceLocal);
	}

	@SuppressWarnings("unchecked")
	private void doObjectRequest(HttpServletRequest request, String indexName,
			boolean forceLocal) throws ServletException {
		StreamReadObject readObject = null;
		try {
			Client client = Client.getWebAppInstance();
			readObject = new StreamReadObject(request.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof List)
				updateDoc(client, indexName,
						(List<? extends IndexDocument>) obj, forceLocal);
			else if (obj instanceof IndexDocument)
				updateDoc(client, indexName, (IndexDocument) obj, forceLocal);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (readObject != null)
				readObject.close();
		}

	}

	private void doXmlRequest(HttpServletRequest request, String indexName,
			boolean forceLocal) throws ServletException {
		try {
			Client client = Client.getWebAppInstance();
			XPathParser xpp = new XPathParser(request.getInputStream());
			NodeList nodeList = xpp.getNodeList("/index/document");
			int l = nodeList.getLength();
			List<IndexDocument> docList = new ArrayList<IndexDocument>();
			for (int i = 0; i < l; i++)
				docList.add(new IndexDocument(xpp, nodeList.item(i)));
			updateDoc(client, indexName, docList, forceLocal);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (XPathExpressionException e) {
			throw new ServletException(e);
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		}

	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		HttpServletRequest request = transaction.getServletRequest();
		String indexName = request.getParameter("index");
		boolean forceLocal = (request.getParameter("forceLocal") != null);
		String ct = request.getContentType();
		if (ct != null && ct.toLowerCase().contains("xml"))
			doXmlRequest(request, indexName, forceLocal);
		else
			doObjectRequest(request, indexName, forceLocal);
		PrintWriter writer;
		try {
			writer = transaction.getWriter("UTF-8");
		} catch (IOException e) {
			throw new ServletException(e);
		}
		writer.println("OK");
	}
}
