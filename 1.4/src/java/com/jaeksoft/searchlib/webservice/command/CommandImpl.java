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
package com.jaeksoft.searchlib.webservice.command;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class CommandImpl extends CommonServices implements SoapCommand,
		RestCommand {

	private Client getClient(String use, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			return client;
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult optimize(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.optimize();
			return new CommonResult(true, "optimize");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult online(String use, String login, String key) {
		Client client = getClient(use, login, key);
		client.setOnline(true);
		return new CommonResult(true, "online");
	}

	@Override
	public CommonResult offline(String use, String login, String key) {
		Client client = getClient(use, login, key);
		client.setOnline(false);
		return new CommonResult(true, "offline");
	}

	@Override
	public CommonResult reload(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.reload();
			return new CommonResult(true, "reload");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult optimizeJSON(String use, String login, String key) {
		return optimize(use, login, key);
	}

	@Override
	public CommonResult optimizeXML(String use, String login, String key) {
		return optimize(use, login, key);
	}

	@Override
	public CommonResult onlineJSON(String use, String login, String key) {
		return online(use, login, key);
	}

	@Override
	public CommonResult onlineXML(String use, String login, String key) {
		return online(use, login, key);
	}

	@Override
	public CommonResult offlineJSON(String use, String login, String key) {
		return offline(use, login, key);
	}

	@Override
	public CommonResult offlineXML(String use, String login, String key) {
		return offline(use, login, key);
	}

	@Override
	public CommonResult reloadJSON(String use, String login, String key) {
		return reload(use, login, key);
	}

	@Override
	public CommonResult reloadXML(String use, String login, String key) {
		return reload(use, login, key);
	}

}
