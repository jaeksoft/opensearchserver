/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.request.IndexRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class IndexServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3855116559376800406L;

	private int updateDoc(Client client, IndexDocument doc)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return client.updateDocument(doc) ? 1 : 0;
	}

	private int updateDoc(Client client,
			Collection<IndexDocument> indexDocuments)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return client.updateDocuments(indexDocuments);
	}

	private int updateDoc(Client client, IndexRequest indexRequest)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return updateDoc(client, indexRequest.getCollection());
	}

	private int doObjectRequest(Client client, ServletTransaction transaction)
			throws ServletException {
		StreamReadObject readObject = null;
		try {
			readObject = new StreamReadObject(transaction.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof IndexRequest)
				return updateDoc(client, (IndexRequest) obj);
			else if (obj instanceof IndexDocument)
				return updateDoc(client, (IndexDocument) obj);
			throw new ServletException("Nothing to do");
		} catch (Exception e) {
			Logging.error(e.getMessage(), e);
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

			String indexName = transaction.getIndexName();

			User user = transaction.getLoggedUser();
			if (user != null && !user.hasRole(indexName, Role.INDEX_UPDATE))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();
			String ct = transaction.getRequestContentType();
			int bufferSize = transaction.getParameterInteger("bufferSize", 50);
			Object result = null;
			if (ct != null && ct.toLowerCase().contains("xml")) {
				InputSource inputSource = new InputSource(
						transaction.getReader());
				result = client.updateXmlDocuments(inputSource, bufferSize,
						null, client.getWebPropertyManager().getProxyHandler());
			} else
				result = doObjectRequest(client, transaction);
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
		} catch (InterruptedException e) {
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
