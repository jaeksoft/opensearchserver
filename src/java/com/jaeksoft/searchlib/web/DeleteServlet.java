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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.request.DeleteRequest;

public class DeleteServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2663934578246659291L;

	private boolean deleteUniqDoc(Client client, String indexName, String uniq)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (indexName == null)
			return client.deleteDocument(uniq);
		else
			return client.deleteDocument(indexName, uniq);
	}

	private boolean deleteDocById(Client client, String indexName, int docId)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (indexName == null)
			return client.deleteDocument(docId);
		else
			return client.deleteDocument(indexName, docId);
	}

	private int deleteUniqDocs(Client client, String indexName,
			Collection<String> uniqFields) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		if (indexName == null)
			return client.deleteDocuments(uniqFields);
		else
			return client.deleteDocuments(indexName, uniqFields);
	}

	private int deleteDocsById(Client client, String indexName,
			Collection<Integer> docIds) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		if (indexName == null)
			return client.deleteDocumentsById(docIds);
		else
			return client.deleteDocumentsbyId(indexName, docIds);
	}

	@SuppressWarnings("unchecked")
	private Object doObjectRequest(HttpServletRequest request,
			String indexName, boolean byId) throws ServletException {
		StreamReadObject readObject = null;
		try {
			Client client = Client.getWebAppInstance();
			readObject = new StreamReadObject(request.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof DeleteRequest) {
				if (byId)
					return deleteDocsById(client, indexName,
							((DeleteRequest<Integer>) obj).getCollection());
				else
					return deleteUniqDocs(client, indexName,
							((DeleteRequest<String>) obj).getCollection());
			} else if (obj instanceof String)
				return deleteUniqDoc(client, indexName, (String) obj);
			else if (obj instanceof Integer)
				return deleteDocById(client, indexName, (Integer) obj);
			return "Error";
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
			String uniq = request.getParameter("uniq");
			String docId = request.getParameter("docId");
			boolean byId = request.getParameter("byId") != null;
			Object result = null;
			if (uniq != null)
				result = deleteUniqDoc(client, indexName, uniq);
			else if (docId != null)
				result = deleteDocById(client, indexName, Integer
						.parseInt(docId));
			else
				result = doObjectRequest(request, indexName, byId);
			PrintWriter pw = transaction.getWriter("UTF-8");
			pw.println(result);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static boolean delete(URI uri, String indexName, String uniqueField)
			throws IOException, URISyntaxException {
		String msg = call(buildUri(uri, "/delete", indexName, "uniq="
				+ uniqueField));
		return Boolean.getBoolean(msg.trim());
	}

	public static int delete(URI uri, String indexName,
			Collection<String> uniqueFields) throws IOException,
			URISyntaxException {
		String msg = sendObject(buildUri(uri, "/delete", indexName, null),
				new DeleteRequest<String>(uniqueFields));
		return Integer.getInteger(msg.trim());
	}

	public static boolean deleteDocument(URI uri, String indexName, int docId)
			throws HttpException, IOException, URISyntaxException {
		String msg = call(buildUri(uri, "/delete", indexName, "id=" + docId));
		return Boolean.getBoolean(msg.trim());
	}

	public static int deleteDocuments(URI uri, String indexName,
			Collection<Integer> docIds) throws IOException, URISyntaxException {
		String msg = sendObject(
				buildUri(uri, "/delete", indexName, "byId=yes"),
				new DeleteRequest<Integer>(docIds));
		return Integer.getInteger(msg.trim());
	}

}
