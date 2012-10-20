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

import java.io.IOException;
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

	private void query(ServletTransaction transaction, Client client, User user)
			throws SearchLibException, IOException {
		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_QUERY))
			throw new SearchLibException("Not permitted");
		Integer rows = transaction.getParameterInteger("rows", 10);
		String query = transaction.getParameterString("query");
		AutoCompletionManager manager = client.getAutoCompletionManager();
		transaction.setResponseContentType("text/plain");
		PrintWriter pw = transaction.getWriter("UTF-8");
		AbstractResultSearch result = manager.search(query, rows);
		if (result == null)
			return;
		if (result.getDocumentCount() <= 0)
			return;
		for (ResultDocument document : result)
			pw.println(document.getValueContent(
					AutoCompletionManager.autoCompletionSchemaFieldTerm, 0));
	}

	private void set(ServletTransaction transaction, Client client, User user)
			throws SearchLibException {
		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_SCHEMA))
			throw new SearchLibException("Not permitted");
		String field = transaction.getParameterString("field");
		AutoCompletionManager autoComp = client.getAutoCompletionManager();
		autoComp.setField(field);
		autoComp.save();
		transaction.addXmlResponse("Status", "OK");
		transaction.addXmlResponse("Field", field);
	}

	private void build(ServletTransaction transaction, Client client, User user)
			throws SearchLibException, IOException {
		if (user != null
				&& !user.hasRole(transaction.getIndexName(), Role.INDEX_UPDATE))
			throw new SearchLibException("Not permitted");
		int bufferSize = transaction.getParameterInteger("bufferSize", 1000);
		int result = client.getAutoCompletionManager().build(14400, bufferSize,
				null);
		transaction.addXmlResponse("Status", "OK");
		transaction.addXmlResponse("Count", Integer.toString(result));

	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			User user = transaction.getLoggedUser();
			Client client = transaction.getClient();
			String cmd = transaction.getParameterString("cmd");
			if ("build".equalsIgnoreCase(cmd))
				build(transaction, client, user);
			else if ("set".equalsIgnoreCase(cmd))
				set(transaction, client, user);
			else
				query(transaction, client, user);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
