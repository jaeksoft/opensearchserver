/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
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
			UrlManager urlManager = client.getUrlManager();
			String cmd = transaction.getParameterString("cmd");
			String host = transaction.getParameterString("host");
			if ("urls".equalsIgnoreCase(cmd)) {
				exportURLs(urlManager, transaction, host);
			} else if ("sitemap".equalsIgnoreCase(cmd)) {
				exportSiteMap(urlManager, transaction, host);
			}
		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (ParseException e) {
			throw new ServletException(e);
		}

	}

	private SearchRequest getRequest(UrlManager urlManager, String host)
			throws SearchLibException, ParseException {
		SearchRequest searchRequest = urlManager
				.getSearchRequest(SearchTemplate.urlExport);
		searchRequest.setQueryString("*:*");
		if (host != null && host.length() > 0)
			searchRequest.addFilter("host:\"" + host + '"', false);
		return searchRequest;
	}

	private void exportSiteMap(UrlManager urlManager,
			ServletTransaction transaction, String host)
			throws SearchLibException, ParseException {
		File file;
		SearchRequest searchRequest = getRequest(urlManager, host);
		file = urlManager.exportSiteMap(searchRequest);
		transaction
				.sendFile(file, "OSS_SiteMap.xml", "text/xml; charset-UTF-8");
	}

	private void exportURLs(UrlManager urlManager,
			ServletTransaction transaction, String host)
			throws SearchLibException, ParseException {
		File file;
		SearchRequest searchRequest = getRequest(urlManager, host);
		file = urlManager.exportURLs(searchRequest);
		transaction.sendFile(file, "OSS_URL_Export.txt",
				"text/plain; charset-UTF-8");
	}
}
