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

import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class AutoCompletionImpl extends CommonServices implements
		SoapAutoCompletion, RestAutoCompletion {

	private AutoCompletionItem getAutoCompItem(Client client, String name)
			throws SearchLibException {
		AutoCompletionItem autoCompItem = client.getAutoCompletionManager()
				.getItem(name);
		if (autoCompItem == null)
			throw new CommonServiceException(Status.NOT_FOUND,
					"Autocompletion item not found: " + name);
		return autoCompItem;
	}

	@Override
	public CommonResult set(String index, String login, String key,
			String name, String field, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(client, name);
			if (field != null)
				autoCompItem.setField(field);
			if (rows != null)
				autoCompItem.setRows(rows);
			autoCompItem.save();
			return new CommonResult(true, null);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult setXML(String index, String login, String key,
			String name, String field, Integer rows) {
		return set(index, login, key, name, field, rows);
	}

	@Override
	public CommonResult setJSON(String index, String login, String key,
			String name, String field, Integer rows) {
		return set(index, login, key, name, field, rows);
	}

	@Override
	public CommonResult build(String index, String login, String key,
			String name) {
		try {
			Client client = getLoggedClient(index, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(client, name);
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

	@Override
	public CommonResult buildXML(String index, String login, String key,
			String name) {
		return build(index, login, key, name);
	}

	@Override
	public CommonResult buildJSON(String index, String login, String key,
			String name) {
		return build(index, login, key, name);
	}

	@Override
	public AutoCompletionResult query(String index, String login, String key,
			String name, String prefix, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(client, name);
			if (rows == null)
				rows = 10;
			return new AutoCompletionResult(autoCompItem.search(prefix, rows));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public AutoCompletionResult queryPost(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}

	@Override
	public AutoCompletionResult queryXML(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}

	@Override
	public AutoCompletionResult queryJSON(String index, String login,
			String key, String name, String prefix, Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}
}
