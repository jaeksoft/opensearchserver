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
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.remote.StreamReadObject;

public class IndexServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3855116559376800406L;

	private void updateDoc(Client client, IndexDocument doc,
			List<? extends IndexDocument> docList, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (doc != null)
			client.getIndex().updateDocument(client.getSchema(), doc,
					forceLocal);
		else if (docList != null)
			client.getIndex().updateDocuments(null, client.getSchema(),
					docList, forceLocal);
	}

	private void updateDoc(Client client, String indexName, IndexDocument doc,
			List<? extends IndexDocument> docList, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (indexName == null) {
			updateDoc(client, doc, docList, forceLocal);
			return;
		}
		if (doc != null)
			client.getIndex().updateDocument(indexName, client.getSchema(),
					doc, forceLocal);
		else if (docList != null)
			client.getIndex().updateDocuments(indexName, client.getSchema(),
					docList, forceLocal);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		StreamReadObject readObject = null;
		try {
			Client client = Client.getWebAppInstance();
			HttpServletRequest request = transaction.getServletRequest();
			readObject = new StreamReadObject(request.getInputStream());
			List<? extends IndexDocument> docList = null;
			IndexDocument doc = null;
			Object obj = readObject.read();
			if (obj instanceof List)
				docList = (List<? extends IndexDocument>) obj;
			else if (obj instanceof IndexDocument)
				doc = (IndexDocument) obj;
			String index = request.getParameter("index");
			boolean forceLocal = (request.getParameter("forceLocal") != null);
			updateDoc(client, index, doc, docList, forceLocal);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (readObject != null)
				readObject.close();
		}

	}
}
