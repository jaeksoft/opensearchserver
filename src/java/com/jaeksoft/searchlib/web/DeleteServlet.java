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

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;

public class DeleteServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2663934578246659291L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			Client client = Client.getWebAppInstance();
			HttpServletRequest request = transaction.getServletRequest();
			String index = request.getParameter("index");
			boolean forceLocal = (request.getParameter("forceLocal") != null);
			String uniq = request.getParameter("uniq");
			if (index == null)
				client.getIndex().deleteDocuments(client.getSchema(), uniq,
						forceLocal);
			else
				client.getIndex().deleteDocuments(index, client.getSchema(),
						uniq, forceLocal);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
