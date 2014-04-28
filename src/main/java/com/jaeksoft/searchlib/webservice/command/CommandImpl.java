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
package com.jaeksoft.searchlib.webservice.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.RestApplication;

public class CommandImpl extends CommonServices implements RestCommand {

	private Client getClient(UriInfo uriInfo, String use, String login,
			String key) {
		try {
			Client client = getLoggedClientAnyRole(uriInfo, use, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			return client;
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult optimize(UriInfo uriInfo, String use, String login,
			String key) {
		try {
			Client client = getClient(uriInfo, use, login, key);
			client.optimize();
			return new CommonResult(true, "optimize");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult truncate(UriInfo uriInfo, String use, String login,
			String key) {
		try {
			Client client = getClient(uriInfo, use, login, key);
			client.deleteAll();
			return new CommonResult(true, "truncate");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult merge(UriInfo uriInfo, String use, String login,
			String key, String index) {
		try {
			Client client = getClient(uriInfo, use, login, key);
			Client sourceClient = getLoggedClientAnyRole(uriInfo, index, login,
					key, Role.GROUP_INDEX);
			client.mergeData(sourceClient);
			return new CommonResult(true, "merge");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult reload(UriInfo uriInfo, String use, String login,
			String key) {
		try {
			Client client = getClient(uriInfo, use, login, key);
			client.reload();
			return new CommonResult(true, "reload");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult optimizeJSON(UriInfo uriInfo, String use, String login,
			String key) {
		return optimize(uriInfo, use, login, key);
	}

	@Override
	public CommonResult optimizeXML(UriInfo uriInfo, String use, String login,
			String key) {
		return optimize(uriInfo, use, login, key);
	}

	public static String getOptimizeXML(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/optimize/{index}/xml",
				user, client);
	}

	public static String getOptimizeJSON(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/optimize/{index}/json",
				user, client);
	}

	@Override
	public CommonResult reloadJSON(UriInfo uriInfo, String use, String login,
			String key) {
		return reload(uriInfo, use, login, key);
	}

	@Override
	public CommonResult reloadXML(UriInfo uriInfo, String use, String login,
			String key) {
		return reload(uriInfo, use, login, key);
	}

	public static String getReloadXML(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/reload/{index}/xml", user,
				client);
	}

	public static String getReloadJSON(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/reload/{index}/json", user,
				client);
	}

	@Override
	public CommonResult truncateJSON(UriInfo uriInfo, String use, String login,
			String key) {
		return truncate(uriInfo, use, login, key);
	}

	@Override
	public CommonResult truncateXML(UriInfo uriInfo, String use, String login,
			String key) {
		return truncate(uriInfo, use, login, key);
	}

	public static String getTruncateXML(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/truncate/{index}/xml",
				user, client);
	}

	public static String getTruncateJSON(User user, Client client)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/truncate/{index}/json",
				user, client);
	}

	@Override
	public CommonResult mergeJSON(UriInfo uriInfo, String use, String login,
			String key, String index) {
		return merge(uriInfo, use, login, key, index);
	}

	@Override
	public CommonResult mergeXML(UriInfo uriInfo, String use, String login,
			String key, String index) {
		return merge(uriInfo, use, login, key, index);
	}

	public static String getMergeXML(User user, Client client, String index)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/merge/{index}/xml", user,
				client, "index", index);
	}

	public static String getMergeJSON(User user, Client client, String index)
			throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/merge/{index}/json", user,
				client, "index", index);
	}
}
