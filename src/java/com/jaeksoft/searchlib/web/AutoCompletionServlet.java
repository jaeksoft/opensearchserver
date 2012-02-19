/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.PrintWriter;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class AutoCompletionServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1432959171606102988L;

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
			Integer rows = transaction.getParameterInteger("rows", 10);
			String query = transaction.getParameterString("query");
			AutoCompletionManager manager = client.getAutoCompletionManager();
			transaction.setResponseContentType("text/plain");
			PrintWriter pw = transaction.getWriter("UTF-8");
			AbstractResultSearch result = manager.search(query, rows);
			if (result == null)
				return;
			ResultDocument[] documents = result.getDocuments();
			if (documents == null)
				return;
			for (ResultDocument document : documents)
				pw.println(document.getValueContent(
						AutoCompletionManager.autoCompletionSchemaFieldTerm, 0));
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}
}
