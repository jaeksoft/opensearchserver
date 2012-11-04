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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;

public class DeleteImpl extends CommonServicesImpl implements Delete {
	public int deletedDocs;

	@Override
	public int delete(String q, String use, String login, String key,
			List<String> uniqueDocs) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = ClientCatalog.getClient(use);
			if (isLogged(use, login, key)) {
				if (q != null && !q.equals(""))
					deletedDocs = deleteByQuery(client, q);

				if (uniqueDocs != null && uniqueDocs.size() > 0) {
					List<String> uniqueList = new ArrayList<String>();
					for (String uniq : uniqueDocs) {
						uniq = uniq.trim();
						if (uniq != null && !uniq.equals(""))
							uniqueList.add(uniq);
					}
					if (uniqueList != null && uniqueList.size() > 0) {
						deletedDocs = deleteUniqDocs(client, uniqueDocs);
					}
				}
				return deletedDocs;
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InstantiationException e) {
			throw new WebServiceException(e);
		} catch (IllegalAccessException e) {
			throw new WebServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new WebServiceException(e);
		} catch (URISyntaxException e) {
			throw new WebServiceException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new WebServiceException(e);
		} catch (ParseException e) {
			throw new WebServiceException(e);
		} catch (SyntaxError e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	private int deleteByQuery(Client client, String q)
			throws SearchLibException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError, URISyntaxException, InterruptedException {
		SearchRequest request = new SearchRequest(client);
		request.setQueryString(q);
		return client.deleteDocuments(request);
	}

	private int deleteUniqDocs(Client client, Collection<String> uniqFields)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return client.deleteDocuments(uniqFields);
	}
}
