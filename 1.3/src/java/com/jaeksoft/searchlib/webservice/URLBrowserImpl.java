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
package com.jaeksoft.searchlib.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;

public class URLBrowserImpl extends CommonServicesImpl implements URLBrowser {

	private SearchRequest getRequest(UrlManager urlManager, String host)
			throws SearchLibException, ParseException {
		SearchRequest searchRequest = urlManager
				.getSearchRequest(SearchTemplate.urlExport);
		searchRequest.setQueryString("*:*");
		if (host != null && host.length() > 0)
			searchRequest.addFilter("host:\"" + host + '"', false);
		return searchRequest;
	}

	@Override
	public byte[] exportURLs(String use, String login, String key) {
		File file;
		byte[] byteArray = null;
		try {
			Client client = ClientCatalog.getClient(use);
			if (isLogged(use, login, key)) {
				file = client.getUrlManager().exportURLs(
						getRequest(client.getUrlManager(), null));
				byteArray = IOUtils.toByteArray(new FileInputStream(file));
			}
		} catch (SearchLibException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	@Override
	public byte[] exportSiteMap(String use, String host, String login,
			String key) {
		File file;
		byte[] byteArray = null;
		try {
			Client client = ClientCatalog.getClient(use);
			if (isLogged(use, login, key)) {
				file = client.getUrlManager().exportSiteMap(
						getRequest(client.getUrlManager(), host));
				byteArray = IOUtils.toByteArray(new FileInputStream(file));
			}
		} catch (SearchLibException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return byteArray;
	}
}
