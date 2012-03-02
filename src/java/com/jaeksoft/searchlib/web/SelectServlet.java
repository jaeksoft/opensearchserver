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
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderSearchJson;
import com.jaeksoft.searchlib.render.RenderSearchXml;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class SelectServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2241064786260022955L;

	protected Render doQueryRequest(Client client,
			ServletTransaction transaction, String render) throws IOException,
			ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {

		SearchRequest searchRequest = (SearchRequest) client
				.getNewRequest(transaction);
		AbstractResultSearch result = (AbstractResultSearch) client
				.request(searchRequest);
		if ("jsp".equalsIgnoreCase(render)) {
			String jsp = transaction.getParameterString("jsp");
			return new RenderJsp(jsp, result);
		} else if ("json".equalsIgnoreCase(render)) {
			String jsonIndent = transaction.getParameterString("indent");
			return new RenderSearchJson(result, jsonIndent);
		} else
			return new RenderSearchXml(result);
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
			Render render = doQueryRequest(client, transaction,
					transaction.getParameterString("format"));
			render.render(transaction);

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

}
