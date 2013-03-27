/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

public class CommonServices {

	private Client client = null;
	private User user = null;

	private void checkClientAndUser(String use, String login, String key)
			throws SearchLibException, NamingException {
		if (!ClientCatalog.getUserList().isEmpty()) {
			user = ClientCatalog.authenticateKey(login, key);
			if (user == null)
				throw new WebServiceException("Authentication failed");
		}
		client = ClientCatalog.getClient(use);
		if (client == null)
			throw new WebServiceException("Index not found");
	}

	protected User getLoggedUser(String login, String key)
			throws SearchLibException {
		if (ClientCatalog.getUserList().isEmpty())
			return null;
		user = ClientCatalog.authenticateKey(login, key);
		if (user == null)
			throw new WebServiceException("Authentication failed");
		return user;
	}

	protected User getLoggedAdmin(String login, String key)
			throws SearchLibException {
		User user = getLoggedUser(login, key);
		if (user == null)
			return null;
		if (!user.isAdmin())
			throw new WebServiceException("Not allowed");
		return user;
	}

	protected Client getLoggedClient(String use, String login, String key,
			Role role) {
		try {
			checkClientAndUser(use, login, key);
			if (user == null)
				return client;
			if (user.hasRole(client.getIndexName(), role))
				return client;
			throw new WebServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}
	}

	protected Client getLoggedClientAnyRole(String use, String login,
			String key, Role... roles) {
		try {
			checkClientAndUser(use, login, key);
			if (user == null)
				return client;
			if (user.hasAnyRole(client.getIndexName(), roles))
				return client;
			throw new WebServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}
	}

	protected Client getLoggedClientAllRoles(String use, String login,
			String key, Role... roles) {
		try {
			checkClientAndUser(use, login, key);
			if (user == null)
				return client;
			if (user.hasAllRole(client.getIndexName(), roles))
				return client;
			throw new WebServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		}
	}
}
