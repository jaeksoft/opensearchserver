/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.autocompletion;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class AutoCompletionCommon extends CommonServices {

	private AutoCompletionItem getAutoCompItem(AutoCompletionManager manager,
			String name) throws SearchLibException, IOException {
		AutoCompletionItem autoCompItem = manager.getItem(name);
		if (autoCompItem == null)
			throw new CommonServiceException(Status.NOT_FOUND,
					"Autocompletion item not found: " + name);
		return autoCompItem;
	}

	protected CommonResult set(String index, String login, String key,
			String name, List<String> fields, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			AutoCompletionItem updateCompItem = manager.getItem(name);
			@SuppressWarnings("resource")
			AutoCompletionItem autoCompItem = updateCompItem == null ? new AutoCompletionItem(
					client, name) : updateCompItem;
			if (fields != null)
				autoCompItem.setFields(fields);
			if (rows != null)
				autoCompItem.setRows(rows);
			if (updateCompItem != null)
				updateCompItem.save();
			else
				manager.add(autoCompItem);
			StringBuffer sb = new StringBuffer("Autocompletion item ");
			sb.append(name);
			sb.append(updateCompItem != null ? " updated." : " inserted");
			return new CommonResult(true, sb.toString());
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	protected CommonResult build(String index, String login, String key,
			String name) {
		try {
			Client client = getLoggedClient(index, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(
					client.getAutoCompletionManager(), name);
			CommonResult result = new CommonResult(true, null);
			autoCompItem.build(86400, 1000, result);
			return result;
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	protected AutoCompletionResult query(String index, String login,
			String key, String name, String prefix, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(
					client.getAutoCompletionManager(), name);
			return new AutoCompletionResult(autoCompItem.search(prefix, rows));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	protected CommonResult delete(String index, String login, String key,
			String name) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			AutoCompletionItem autoCompItem = getAutoCompItem(manager, name);
			manager.delete(autoCompItem);
			return new CommonResult(true, "Autocompletion item " + name
					+ " deleted");
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	protected CommonListResult list(String index, String login, String key) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			return new CommonListResult(manager.getItems());
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

}
