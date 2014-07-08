/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XPathParser;

public class DeleteServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2663934578246659291L;

	private int deleteUniqDoc(Client client, String field, String value)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		return client.deleteDocument(field, value);

	}

	private int deleteByQuery(Client client, String q)
			throws SearchLibException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError, URISyntaxException, InterruptedException {
		AbstractSearchRequest request = new SearchPatternRequest(client);
		request.setQueryString(q);
		return client.deleteDocuments(request);
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

			String uniq = transaction.getParameterString("uniq");
			String q = transaction.getParameterString("q");
			Integer result = null;
			if (uniq != null)
				result = deleteUniqDoc(client, null, uniq);
			else if (q != null)
				result = deleteByQuery(client, q);
			transaction.addXmlResponse("Status", "OK");
			transaction.addXmlResponse("Deleted", result.toString());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static boolean delete(URI uri, String indexName, String login,
			String apikey, String uniqueField, int secTimeOut)
			throws SearchLibException {
		try {
			XPathParser xpp = call(
					secTimeOut,
					buildUri(uri, "/delete", indexName, login, apikey, "uniq="
							+ uniqueField));
			return "OK".equals(xpp
					.getNodeString("/response/entry[@key='Status'"));
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

	public static boolean deleteDocument(URI uri, String indexName,
			String login, String apikey, int docId, int secTimeOut)
			throws SearchLibException {
		try {
			XPathParser xpp = call(
					secTimeOut,
					buildUri(uri, "/delete", indexName, login, apikey, "id="
							+ docId));
			return "OK".equals(xpp
					.getNodeString("/response/entry[@key='Status'"));
		} catch (SearchLibException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

}
