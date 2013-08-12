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
package com.jaeksoft.searchlib.webservice.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class FieldImpl extends CommonServices implements SoapField, RestField {

	@Override
	public CommonResult setField(String use, String login, String key,
			SchemaFieldRecord schemaFieldRecord) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			if (schemaFieldRecord == null)
				throw new CommonServiceException(Status.BAD_REQUEST,
						"The field structure is missing");
			SchemaField schemaField = new SchemaField();
			schemaFieldRecord.toShemaField(schemaField);
			client.getSchema().getFieldList().put(schemaField);
			client.saveConfig();
			return new CommonResult(true, "Added Field "
					+ schemaFieldRecord.name);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult deleteField(String use, String login, String key,
			String deleteField) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			String message = delete(client, use, deleteField);
			return new CommonResult(true, message);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	private String delete(Client client, String use, String deleteField)
			throws SearchLibException {
		com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
		SchemaFieldList sfl = schema.getFieldList();
		SchemaField field = sfl.get(deleteField.trim());
		if (field == null)
			throw new CommonServiceException(Status.NOT_FOUND,
					"Field not found: " + deleteField);
		sfl.remove(field.getName());
		client.saveConfig();
		return "Deleted " + deleteField;
	}

	@Override
	public CommonResult setDefaultUniqueField(String use, String login,
			String key, String defaultField, String uniqueField) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			SchemaFieldList schemaFieldList = client.getSchema().getFieldList();

			StringBuffer msg = new StringBuffer();

			if (defaultField != null) {
				if (defaultField.length() > 0)
					if (schemaFieldList.get(defaultField) == null)
						throw new CommonServiceException(Status.NOT_FOUND,
								"Field not found: " + defaultField);
				schemaFieldList.setDefaultField(defaultField);
				msg.append("Default field set to '");
				msg.append(defaultField);
				msg.append("'. ");
			}
			if (uniqueField != null) {
				if (uniqueField.length() > 0)
					if (schemaFieldList.get(uniqueField) == null)
						throw new CommonServiceException(Status.NOT_FOUND,
								"Field not found: " + uniqueField);
				schemaFieldList.setUniqueField(uniqueField);
				msg.append("Unique field set to '");
				msg.append(uniqueField);
				msg.append("'.");
			}
			client.saveConfig();
			return new CommonResult(true, msg.toString().trim());
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public ResultFieldList getFieldList(String use, String login, String key) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			List<SchemaFieldRecord> fieldList = new ArrayList<SchemaFieldRecord>();
			SchemaFieldList schemaFieldList = schema.getFieldList();
			for (SchemaField schemaField : schemaFieldList.getList())
				fieldList.add(new SchemaFieldRecord(schemaField));
			return new ResultFieldList(true, fieldList,
					schemaFieldList.getUniqueField(),
					schemaFieldList.getDefaultField());
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public ResultField getFieldList(String use, String login, String key,
			String field) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			SchemaField schemaField = schema.getFieldList().get(field);
			if (schemaField == null)
				throw new CommonServiceException(Status.NOT_FOUND,
						"Field not found: " + field);
			return new ResultField(true, schemaField);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		}
	}
}
