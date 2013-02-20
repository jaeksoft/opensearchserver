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
package com.jaeksoft.searchlib.webservice.delete;

import java.io.IOException;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class DeleteImpl extends CommonServices implements SoapDelete,
		RestDelete {

	@Override
	public CommonResult deleteByQuery(String use, String login, String key,
			String query) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			SearchRequest request = new SearchRequest(client);
			request.setQueryString(query);
			int count = client.deleteDocuments(request);
			return new CommonResult(true, count + " document(s) deleted");
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult deleteByValue(String use, String login, String key,
			String field, List<String> values) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			int count = client.deleteDocuments(field, values);
			return new CommonResult(true, count + " document(s) deleted");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult deleteByValueXML(String use, String login, String key,
			String field, List<String> values) {
		return deleteByValue(use, login, key, field, values);
	}

	@Override
	public CommonResult deleteByValueJSON(String use, String login, String key,
			String field, List<String> values) {
		return deleteByValue(use, login, key, field, values);
	}

	@Override
	public CommonResult deleteByQueryXML(String use, String login, String key,
			String query) {
		return deleteByQuery(use, login, key, query);
	}

	@Override
	public CommonResult deleteByQueryJSON(String use, String login, String key,
			String query) {
		return deleteByQuery(use, login, key, query);
	}
}
