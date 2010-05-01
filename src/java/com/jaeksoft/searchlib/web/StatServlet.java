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

import java.io.PrintWriter;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;

@Deprecated
public class StatServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6835267443840241748L;

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {

			HttpServletRequest request = servletTransaction.getServletRequest();
			Client client = ClientCatalog.getClient(request);

			String reload = request.getParameter("reload");

			if (reload != null)
				client.reload(reload);

			PrintWriter writer = servletTransaction.getWriter("UTF-8");
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			HashSet<String> classDetail = new HashSet<String>();
			String[] values = request.getParameterValues("details");
			if (values != null)
				for (String value : values)
					classDetail.add(value);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
