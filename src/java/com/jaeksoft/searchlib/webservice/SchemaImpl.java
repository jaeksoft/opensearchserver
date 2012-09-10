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
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.SchemaServlet;

public class SchemaImpl extends CommonServicesImpl implements Schema {

	@Override
	public CommonResult deleteIndex(String login, String key, String indexName) {

		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (getAuthentication(login, key)) {
				User user = getUser(login, key);
				ClientCatalog.eraseIndex(user, indexName);
				return new CommonResult(true, "Deleted Index " + indexName);
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		}
		return new CommonResult(false, "Something went wrong");
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			TemplateList indexTemplateName) {
		try {
			if (getAuthentication(login, key)) {
				TemplateAbstract template = TemplateList
						.findTemplate(indexTemplateName.name());
				ClientCatalog.createIndex(null, indexName, template);
				return new CommonResult(true, "Created Index " + indexName);
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		return new CommonResult(false, "Something went wrong");
	}

	private User getUser(String login, String key) throws SearchLibException {
		return ClientCatalog.authenticateKey(login, key);
	}

	private Boolean getAuthentication(String login, String key) {
		try {
			User user = getUser(login, key);
			if (ClientCatalog.getUserList().isEmpty()) {
				return true;
			}
			if (user != null && user.isAdmin()) {
				return true;
			}
		} catch (SearchLibException e) {
			new WebServiceException(e);
		}
		return false;

	}

	@Override
	public List<String> indexList(String login, String key) {
		List<String> indexList = new ArrayList<String>();
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (getAuthentication(login, key)) {
				User user = getUser(login, key);
				for (ClientCatalogItem catalogItem : ClientCatalog
						.getClientCatalog(user)) {
					indexList.add(catalogItem.getIndexName());
				}
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		return indexList;
	}

	@Override
	public CommonResult setField(String use, String login, String key,
			SchemaFieldRecord schemaFieldRecord) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (isLoggedSchema(use, login, key)) {
				Client client = ClientCatalog.getClient(use);
				setField(client, schemaFieldRecord);
				return new CommonResult(true, "Added Field "
						+ schemaFieldRecord.name);
			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		return new CommonResult(false, "Something went wrong");
	}

	private void setField(Client client, SchemaFieldRecord schemaFieldRecord)
			throws SearchLibException {
		com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
		SchemaField schemaField = new SchemaField();
		schemaField.setIndexAnalyzer(schemaFieldRecord.indexAnalyzer);
		schemaField.setIndexed(schemaFieldRecord.indexed);
		schemaField.setName(schemaFieldRecord.name);
		schemaField.setStored(schemaFieldRecord.stored);
		schemaField.setTermVector(schemaFieldRecord.termVector);
		schema.getFieldList().put(schemaField);
		SchemaServlet.saveSchema(client, schema);
	}

	@Override
	public CommonResult deletefield(String use, String login, String key,
			String deleteField) {
		String message = null;
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = ClientCatalog.getClient(use);
			if (isLoggedSchema(use, login, key))
				message = delete(client, use, deleteField);
			else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		if (message != null)
			return new CommonResult(true, message);
		else
			return new CommonResult(false, "Something went wrong");
	}

	private String delete(Client client, String use, String deleteField)
			throws SearchLibException {
		com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
		SchemaFieldList sfl = schema.getFieldList();
		SchemaField field = sfl.get(deleteField.trim());
		if (field == null)
			return "Nothing to delete";
		sfl.remove(field.getName());
		SchemaServlet.saveSchema(client, schema);
		return "Deleted " + deleteField;
	}

	private Client getClient(String use) throws SearchLibException,
			NamingException {
		return ClientCatalog.getClient(use);
	}

	@Override
	public CommonResult setDefaultField(String use, String login, String key,
			String defaultField) {
		String message = null;
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getClient(use);
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			if (isLoggedSchema(use, login, key)) {
				if (defaultField != null
						&& !defaultField.trim().equalsIgnoreCase("")) {
					schema.getFieldList().setDefaultField(defaultField);
					SchemaServlet.saveSchema(client, schema);
					message = "Default Field has been set to " + defaultField;
				}
			}
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		if (message != null)
			return new CommonResult(true, message);
		else
			return new CommonResult(false, "Something went wrong");
	}

	@Override
	public CommonResult setUniqueField(String use, String login, String key,
			String uniqueField) {
		String message = null;
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getClient(use);
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			if (isLoggedSchema(use, login, key)) {
				if (uniqueField != null
						&& !uniqueField.trim().equalsIgnoreCase("")) {
					schema.getFieldList().setUniqueField(uniqueField);
					SchemaServlet.saveSchema(client, schema);
					message = "Unique Field has been set to " + uniqueField;
				}
			}
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		if (message != null)
			return new CommonResult(true, message);
		else
			return new CommonResult(false, "Something went wrong");
	}

	@Override
	public List<SchemaFieldRecord> getFieldList(String use, String login,
			String key) {
		List<SchemaFieldRecord> fieldList = new ArrayList<SchemaFieldRecord>();
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			if (isLoggedSchema(use, login, key)) {
				Client client = getClient(use);
				com.jaeksoft.searchlib.schema.Schema schema = client
						.getSchema();
				for (SchemaField schemaField : schema.getFieldList().getList()) {
					SchemaFieldRecord fieldListResult = new SchemaFieldRecord(
							schemaField.getName(),
							schemaField.getIndexAnalyzer(),
							schemaField.getIndexed(), schemaField.getStored(),
							schemaField.getTermVector());
					fieldList.add(fieldListResult);
				}

			} else
				throw new WebServiceException("Bad Credential");
		} catch (SearchLibException e) {
			new WebServiceException(e);
		} catch (NamingException e) {
			new WebServiceException(e);
		} catch (InterruptedException e) {
			new WebServiceException(e);
		} catch (IOException e) {
			new WebServiceException(e);
		}
		return fieldList;
	}

}
