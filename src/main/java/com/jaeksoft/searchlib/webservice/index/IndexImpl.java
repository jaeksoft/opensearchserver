/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexType;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class IndexImpl extends CommonServices implements RestIndex {

	@Override
	public CommonResult deleteIndex(String login, String key, String indexName) {
		try {
			User user = getLoggedAdmin(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ClientCatalog.eraseIndex(user, indexName);
			return new CommonResult(true, "Index deleted: " + indexName);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (NamingException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			TemplateList indexTemplateName, IndexType indexType,
			String remoteURI) {
		try {
			User user = getLoggedAdmin(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			if (user != null && !user.isAdmin())
				throw new CommonServiceException("Not allowed");
			TemplateAbstract template = TemplateList
					.findTemplate(indexTemplateName.name());
			ClientCatalog.createIndex(user, indexName, template, indexType,
					remoteURI != null ? new URI(remoteURI) : null);
			return new CommonResult(true, "Created Index " + indexName);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			TemplateList indexTemplateName, String remoteURI) {
		return createIndex(login, key, indexName, IndexType.LUCENE, remoteURI);
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			IndexType type, String remoteURI) {
		return createIndex(login, key, indexName, TemplateList.EMPTY_INDEX,
				type, remoteURI);
	}

	@Override
	public ResultIndexList indexList(String login, String key, Boolean details) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			List<String> indexList = new ArrayList<String>();
			ClientCatalog.populateIndexName(user, indexList, null);
			Map<String, IndexInfo> indexMap = null;
			if (details != null && details) {
				indexMap = new TreeMap<String, IndexInfo>();
				for (String indexName : indexList)
					indexMap.put(indexName, new IndexInfo(
							new ClientCatalogItem(indexName)));
				indexList = null;
			}
			return new ResultIndexList(true, indexList, indexMap);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public ResultIndex getIndex(String login, String key, String name,
			Boolean infos) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			if (!ClientCatalog.exists(user, name))
				throw new CommonServiceException(Status.NOT_FOUND, "The index "
						+ name + " has not been found");
			ClientCatalogItem clientCatalogItem = new ClientCatalogItem(name);
			if (infos != null && infos)
				clientCatalogItem.computeInfos();
			return new ResultIndex(clientCatalogItem);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult indexExists(String login, String key, String name) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			if (!ClientCatalog.exists(user, name))
				throw new CommonServiceException(Status.NOT_FOUND, "The index "
						+ name + " has not been found");
			return new CommonResult(true, Boolean.toString(true));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult closeIndex(String login, String key, String indexName) {
		try {
			getLoggedClientAnyRole(indexName, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			ClientCatalog.closeClient(indexName);
			return new CommonResult(true, "Index closed: " + indexName);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

}
