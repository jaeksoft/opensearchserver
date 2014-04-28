/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

import java.net.URI;
import java.net.URISyntaxException;

import javax.naming.NamingException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cluster.ClusterInstance;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;

public class CommonServices {

	protected Client client = null;
	protected User user = null;

	private final void checkClientAndUser(UriInfo uriInfo, String use,
			String login, String key) throws SearchLibException,
			NamingException, RedirectionException, URISyntaxException {
		if (!ClientCatalog.getUserList().isEmpty()) {
			user = ClientCatalog.authenticateKey(login, key);
			if (user == null)
				throw new CommonServiceException("Authentication failed");
		}
		client = ClientCatalog.getLoadedClient(use);
		if (client != null)
			return;
		ClusterInstance instance = ClientCatalog.getAnyClusterInstance(use);
		if (instance != null) {
			URI uri = instance.getUri();
			String src = uriInfo.getRequestUri().toString()
					.substring(uriInfo.getBaseUri().toString().length());
			uri = new URI(StringUtils.fastConcat(uri.toString(),
					"/services/rest", src));
			throw new RedirectionException(Response.Status.TEMPORARY_REDIRECT,
					uri);
		}
		client = ClientCatalog.getClient(use);
		if (client == null)
			throw new CommonServiceException("Index not found");
	}

	protected final User getLoggedUser(String login, String key)
			throws SearchLibException {
		if (ClientCatalog.getUserList().isEmpty())
			return null;
		user = ClientCatalog.authenticateKey(login, key);
		if (user == null)
			throw new CommonServiceException("Authentication failed");
		return user;
	}

	protected final User getLoggedAdmin(String login, String key)
			throws SearchLibException {
		User user = getLoggedUser(login, key);
		if (user == null)
			return null;
		if (!user.isAdmin())
			throw new CommonServiceException("Not allowed");
		return user;
	}

	protected final Client getLoggedClient(UriInfo uriInfo, String use,
			String login, String key, Role role) throws RedirectionException {
		try {
			checkClientAndUser(uriInfo, use, login, key);
			if (user == null)
				return client;
			if (user.hasRole(client.getIndexName(), role))
				return client;
			throw new CommonServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (NamingException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	protected final Client getLoggedClientAnyRole(UriInfo uriInfo, String use,
			String login, String key, Role... roles)
			throws RedirectionException {
		try {
			checkClientAndUser(uriInfo, use, login, key);
			if (user == null)
				return client;
			if (user.hasAnyRole(client.getIndexName(), roles))
				return client;
			throw new CommonServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (NamingException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	protected final Client getLoggedClientAllRoles(UriInfo uriInfo, String use,
			String login, String key, Role... roles)
			throws RedirectionException {
		try {
			checkClientAndUser(uriInfo, use, login, key);
			if (user == null)
				return client;
			if (user.hasAllRole(client.getIndexName(), roles))
				return client;
			throw new CommonServiceException("Not allowed");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (NamingException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	public static class CommonServiceException extends WebApplicationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4426477879345381067L;

		public CommonServiceException(Response.Status status, String message) {
			super(Response.status(status).entity(message)
					.type(MediaType.TEXT_PLAIN).build());
			Logging.warn(message);
		}

		public CommonServiceException(Response.Status status, Exception e) {
			this(status, new ErrorParserLogger.ErrorInfo(e).toString());
		}

		public CommonServiceException(String message) {
			this(Response.Status.BAD_REQUEST, message);
		}

		public CommonServiceException(Exception e) {
			this(Response.Status.INTERNAL_SERVER_ERROR, e);
		}
	}

}
