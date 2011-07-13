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

import java.io.File;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

/**
 * @author Naveen
 * 
 */
public class URLBrowserServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -318303190794379678L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			String indexName = transaction.getIndexName();
			User user = transaction.getLoggedUser();
			if (user != null && !user.hasRole(indexName, Role.INDEX_UPDATE))
				throw new SearchLibException("Not permitted");
			Client client = transaction.getClient();
			String cmd = transaction.getParameterString("cmd");
			String host = transaction.getParameterString("host");
			if ("urls".equalsIgnoreCase(cmd)) {
				exportURLs(client, transaction, host);
			} else if ("sitemap".equalsIgnoreCase(cmd)) {
				exportSiteMap(client, transaction, host);
			}
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		}

	}

	private void exportSiteMap(Client client, ServletTransaction transaction,
			String host) {
		File file;
		try {
			file = client.getUrlManager().exportSiteMap(client, host);
			transaction.sendFile(file, "OSS_SiteMap.xml",
					"text/xml; charset-UTF-8");
		} catch (SearchLibException e) {
			e.printStackTrace();
		}

	}

	private void exportURLs(Client client, ServletTransaction transaction,
			String host) {
		File file;
		try {
			file = client.getUrlManager().exportURLs(client, host);
			transaction.sendFile(file, "OSS_SiteMap.txt",
					"text/xml; charset-UTF-8");
		} catch (SearchLibException e) {
			e.printStackTrace();
		}

	}
}
