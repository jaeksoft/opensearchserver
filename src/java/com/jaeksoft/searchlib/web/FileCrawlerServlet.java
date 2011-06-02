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
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class FileCrawlerServlet extends WebCrawlerServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3367169960498597933L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {

			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.FILE_CRAWLER_START_STOP))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();
			doCrawlMaster(client.getFileCrawlMaster(), transaction);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
