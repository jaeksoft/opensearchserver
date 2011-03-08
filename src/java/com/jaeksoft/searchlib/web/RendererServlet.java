/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class RendererServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9214023062084084833L;

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

			Renderer renderer = client.getRendererManager().get(
					transaction.getParameterString("name"));
			if (renderer == null)
				throw new SearchLibException("The renderer has not been found");
			String query = transaction.getParameterString("query");
			if (query == null)
				throw new SearchLibException("There is not query");
			SearchRequest request = client.getNewSearchRequest(renderer
					.getRequestName());
			if (request == null)
				throw new SearchLibException("No request has been found");
			request.setQueryString(query);
			Result result = client.search(request);

			transaction.setRequestAttribute("result", result);
			transaction.setRequestAttribute("renderer", renderer);
			transaction.forward("/jsp/renderer.jsp");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
