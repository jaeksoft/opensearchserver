/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XmlWriter;

public class MonitorServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6835267443840241748L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {

		try {

			User user = transaction.getLoggedUser();
			if (user != null && !user.isAdmin() && !user.isMonitoring())
				throw new SearchLibException("Not permitted");

			transaction.getServletResponse().setContentType("text/xml");
			XmlWriter xmlWriter = new XmlWriter(transaction.getWriter("UTF-8"),
					"UTF-8");
			xmlWriter.startElement("response");
			new Monitor().writeXmlConfig(xmlWriter);
			xmlWriter.endElement();

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
