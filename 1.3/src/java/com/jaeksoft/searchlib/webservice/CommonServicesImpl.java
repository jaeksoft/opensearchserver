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

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class CommonServicesImpl implements CommonServices {

	@Override
	public Boolean isLogged(String use, String login, String key) {

		try {
			User user = ClientCatalog.authenticateKey(login, key);
			Client client = ClientCatalog.getClient(use);

			if (user == null && ClientCatalog.getUserList().isEmpty())
				return true;

			if (user != null
					&& user.hasRole(client.getIndexName(), Role.INDEX_QUERY))
				return true;

			return false;

		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}

	}

	@Override
	public Boolean isLoggedSchema(String use, String login, String key) {
		try {
			User user = ClientCatalog.authenticateKey(login, key);
			Client client = ClientCatalog.getClient(use);

			if (user == null && ClientCatalog.getUserList().isEmpty())
				return true;
			if (user != null
					&& user.hasRole(client.getIndexName(), Role.INDEX_SCHEMA))
				return true;

			return false;

		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public Boolean isLoggedWebStartStop(String use, String login, String key) {
		try {
			User user = ClientCatalog.authenticateKey(login, key);
			Client client = ClientCatalog.getClient(use);

			if (user == null && ClientCatalog.getUserList().isEmpty())
				return true;
			if (user != null
					&& user.hasRole(client.getIndexName(),
							Role.WEB_CRAWLER_START_STOP))
				return true;

			return false;

		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}
	}
}
