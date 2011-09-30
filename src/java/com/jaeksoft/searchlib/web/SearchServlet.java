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

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderOpenSearch;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class SearchServlet extends AbstractServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4440539263609727717L;

	private Render doQueryRequest(Client client,
			ServletTransaction transaction, String serverURL)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {
		SearchRequest searchRequest = client.getNewSearchRequest(transaction);
		Result result = client.search(searchRequest);
		return new RenderOpenSearch(result, serverURL);
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.INDEX_QUERY))
				throw new SearchLibException("Not permitted");
			Client client = transaction.getClient();
			Render render = null;
			render = doQueryRequest(client, transaction, serverURL);
			render.render(transaction);
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}
}
