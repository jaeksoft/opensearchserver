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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.request.DeleteRequest;

public class DeleteServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2663934578246659291L;

	private void deleteDoc(Client client, String indexName, String uniq)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		if (indexName == null)
			client.deleteDocument(uniq);
		else
			client.deleteDocument(indexName, uniq);
	}

	private void deleteDocs(Client client, String indexName,
			Collection<String> uniqFields) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		if (indexName == null)
			client.deleteDocuments(uniqFields);
		else
			client.deleteDocuments(indexName, uniqFields);
	}

	private void deleteDocs(Client client, String indexName,
			DeleteRequest deleteRequest) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		deleteDocs(client, indexName, deleteRequest.getCollection());
	}

	private void doObjectRequest(HttpServletRequest request, String indexName)
			throws ServletException {
		StreamReadObject readObject = null;
		try {
			Client client = Client.getWebAppInstance();
			readObject = new StreamReadObject(request.getInputStream());
			Object obj = readObject.read();
			if (obj instanceof DeleteRequest)
				deleteDocs(client, indexName, (DeleteRequest) obj);
			else if (obj instanceof String)
				deleteDoc(client, indexName, (String) obj);
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

			if (uniq != null)
				deleteDoc(client, indexName, uniq);
			else
				doObjectRequest(request, indexName);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static void delete(URI uri, String indexName, String uniqueField)
			throws IOException, URISyntaxException {
		call(buildUri(uri, "/delete", indexName, "uniq=" + uniqueField));
	}

	public static void delete(URI uri, String indexName,
			Collection<String> uniqueFields) throws IOException,
			URISyntaxException {
		sendObject(buildUri(uri, "/delete", indexName, null),
				new DeleteRequest(uniqueFields));
	}
}
