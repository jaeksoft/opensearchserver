/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.user.Role;

public abstract class AbstractDirectoryImpl<V, T extends AbstractDirectoryManager<V>>
		extends CommonServices {

	protected abstract T getManager(Client client) throws SearchLibException;

	public CommonListResult list(UriInfo uriInfo, String index, String login,
			String key) {
		try {
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractDirectoryManager<?> manager = getManager(client);
			return new CommonListResult(manager.getList());
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult set(UriInfo uriInfo, String index, String login,
			String key, String name, V content) {
		try {
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractDirectoryManager<V> manager = getManager(client);
			boolean bExists = manager.exists(name);
			manager.saveContent(name, content);
			client.getSchema().recompileAnalyzers();
			client.reload();
			return new CommonResult(true, bExists ? "Item " + name + " updated"
					: "Item " + name + " created");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	protected V get(String name) throws SearchLibException, IOException {
		AbstractDirectoryManager<V> manager = getManager(client);
		if (!manager.exists(name))
			throw new CommonServiceException(Status.NOT_FOUND, "The item "
					+ name + " does not exist");
		return manager.getContent(name);
	}

	public V get(UriInfo uriInfo, String index, String login, String key,
			String name) {
		try {
			getLoggedClient(uriInfo, index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			return get(name);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult exists(UriInfo uriInfo, String index, String login,
			String key, String name) {
		try {
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractDirectoryManager<?> manager = getManager(client);
			if (!manager.exists(name))
				throw new CommonServiceException(Status.NOT_FOUND, "The item "
						+ name + " does not exist");
			return new CommonResult(true, "The item " + name + " exists");
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	public CommonResult delete(UriInfo uriInfo, String index, String login,
			String key, String name) {
		try {
			Client client = getLoggedClient(uriInfo, index, login, key,
					Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractDirectoryManager<V> manager = getManager(client);
			if (!manager.exists(name))
				throw new CommonServiceException(Status.NOT_FOUND, "The item "
						+ name + " does not exist");
			manager.delete(name);
			client.getSchema().recompileAnalyzers();
			client.reload();
			return new CommonResult(true, "The item " + name
					+ " has been deleted");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

}
