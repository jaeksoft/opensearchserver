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
package com.jaeksoft.searchlib.webservice.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class IndexImpl extends CommonServices implements SoapIndex, RestIndex {

	@Override
	public CommonResult deleteIndex(String login, String key, String indexName) {
		try {
			User user = getLoggedAdmin(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ClientCatalog.eraseIndex(user, indexName);
			return new CommonResult(true, "Index deleted: " + indexName);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult deleteIndexXML(String login, String key,
			String indexName) {
		return deleteIndex(login, key, indexName);
	}

	@Override
	public CommonResult deleteIndexJSON(String login, String key,
			String indexName) {
		return deleteIndex(login, key, indexName);
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			TemplateList indexTemplateName) {
		try {
			User user = getLoggedAdmin(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			if (user != null && !user.isAdmin())
				throw new WebServiceException("Not allowed");
			TemplateAbstract template = TemplateList
					.findTemplate(indexTemplateName.name());
			ClientCatalog.createIndex(user, indexName, template);
			return new CommonResult(true, "Created Index " + indexName);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult createIndexXML(String login, String key,
			String indexName, TemplateList indexTemplateName) {
		return createIndex(login, key, indexName, indexTemplateName);
	}

	@Override
	public CommonResult createIndexJSON(String login, String key,
			String indexName, TemplateList indexTemplateName) {
		return createIndex(login, key, indexName, indexTemplateName);
	}

	@Override
	public ResultIndexList indexList(String login, String key) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			List<String> indexList = new ArrayList<String>();
			for (ClientCatalogItem catalogItem : ClientCatalog
					.getClientCatalog(user))
				indexList.add(catalogItem.getIndexName());
			return new ResultIndexList(true, indexList);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public ResultIndexList indexListXML(String login, String key) {
		return indexList(login, key);
	}

	@Override
	public ResultIndexList indexListJSON(String login, String key) {
		return indexList(login, key);
	}

	@Override
	public CommonResult indexExists(String login, String key, String name) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			return new CommonResult(true, Boolean.toString(ClientCatalog
					.exists(user, name)));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult indexExistsXML(String login, String key, String name) {
		return indexExists(login, key, name);
	}

	@Override
	public CommonResult indexExistsJSON(String login, String key, String name) {
		return indexExists(login, key, name);
	}
}
