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

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexMode;

public class ActionImpl extends CommonServicesImpl implements Action {

	@Override
	public CommonResult action(String use, String login, String key,
			Boolean optimize, Boolean reload, Boolean online, Boolean offline,
			Boolean readonly, Boolean readwrite) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (isLogged(use, login, key)) {

				Client client = ClientCatalog.getClient(use);
				if (optimize != null && optimize == true) {
					optimize(client);
					return new CommonResult(true, "optimized");
				}
				if (reload != null && reload == true) {
					reload(client);
					return new CommonResult(true, "reloaded");
				}
				if (online != null && online == true) {
					online(client);
					return new CommonResult(true, "online");
				}
				if (offline != null && offline == true) {
					offline(client);
					return new CommonResult(true, "offline ");
				}
				if (readonly != null && readonly == true) {
					readOnly(client);
					return new CommonResult(true, "readonly");
				}
				if (readwrite != null && readwrite == true) {
					readWrite(client);
					return new CommonResult(true, "readwrite");
				}
			}
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (URISyntaxException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InstantiationException e) {
			throw new WebServiceException(e);
		} catch (IllegalAccessException e) {
			throw new WebServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new WebServiceException(e);
		} catch (HttpException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
		return new CommonResult(false, "Something went Wrong");
	}

	public static void optimize(Client client) throws SearchLibException,
			URISyntaxException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		client.optimize();
	}

	public static void reload(Client client) throws SearchLibException,
			URISyntaxException {
		client.reload();
	}

	public static void online(Client client) throws SearchLibException,
			URISyntaxException {
		client.setOnline(true);
	}

	public static void offline(Client client) throws SearchLibException,
			URISyntaxException {
		client.setOnline(false);
	}

	public static void readOnly(Client client) throws SearchLibException,
			URISyntaxException {
		client.setReadWriteMode(IndexMode.READ_ONLY);
	}

	public static void readWrite(Client client) throws SearchLibException,
			URISyntaxException {
		client.setReadWriteMode(IndexMode.READ_WRITE);
	}

}
