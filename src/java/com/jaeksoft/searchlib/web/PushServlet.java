/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import javax.naming.NamingException;
import javax.servlet.ServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;

public class PushServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 527058083952741700L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		ServletRequest request = transaction.getServletRequest();
		String indexName = request.getParameter("indexName");
		long version = Long.parseLong(request.getParameter("version"));
		String fileName = request.getParameter("fileName");
		try {
			Client client = ClientCatalog.getClient(request);
			client.getIndex().receive(indexName, version, fileName,
					request.getInputStream());
		} catch (SearchLibException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch (NamingException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}
}
