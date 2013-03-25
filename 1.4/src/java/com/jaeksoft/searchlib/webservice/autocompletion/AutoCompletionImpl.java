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

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class AutoCompletionImpl extends CommonServices implements
		SoapAutoCompletion, RestAutoCompletion {

	@Override
	public CommonResult set(String index, String login, String key,
			String field, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			if (field != null)
				manager.setField(field);
			if (rows != null)
				manager.setRows(rows);
			manager.save();
			return new CommonResult(true, null);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult setXML(String index, String login, String key,
			String field, Integer rows) {
		return set(index, login, key, field, rows);
	}

	@Override
	public CommonResult setJSON(String index, String login, String key,
			String field, Integer rows) {
		return set(index, login, key, field, rows);
	}

	@Override
	public CommonResult build(String index, String login, String key) {
		try {
			Client client = getLoggedClient(index, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			CommonResult result = new CommonResult(true, null);
			manager.build(86400, 1000, result);
			return result;
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult buildXML(String index, String login, String key) {
		return build(index, login, key);
	}

	@Override
	public CommonResult buildJSON(String index, String login, String key) {
		return build(index, login, key);
	}

	@Override
	public AutoCompletionResult query(String index, String login, String key,
			String prefix, Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			if (rows == null)
				rows = 10;
			return new AutoCompletionResult(manager.search(prefix, rows));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public AutoCompletionResult queryXML(String index, String login,
			String key, String prefix, Integer rows) {
		return query(index, login, key, prefix, rows);
	}

	@Override
	public AutoCompletionResult queryJSON(String index, String login,
			String key, String prefix, Integer rows) {
		return query(index, login, key, prefix, rows);
	}
}
