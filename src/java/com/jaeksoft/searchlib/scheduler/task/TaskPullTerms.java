/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;

public class TaskPullTerms extends TaskAbstract {

	final private TaskPropertyDef propSourceIndex = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Index source", 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login (Index target)", 20);

	final private TaskPropertyDef propApiKey = new TaskPropertyDef(
			TaskPropertyType.password, "API Key (Index target)", 50);

	final private TaskPropertyDef propSourceField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source field name", 50);

	final private TaskPropertyDef propTermField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Term field name", 50);

	final private TaskPropertyDef propFreqField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Frequency field name", 50);

	final private TaskPropertyDef propFreqPadSize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Frequency pad", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceField,
			propSourceIndex, propLogin, propApiKey, propTermField,
			propFreqField, propFreqPadSize };

	@Override
	public String getName() {
		return "Pull terms";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		List<String> values = new ArrayList<String>(0);
		if (propertyDef == propSourceIndex) {
			for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null)) {
				String v = item.getIndexName();
				if (!v.equals(config.getIndexName()))
					values.add(v);
			}
		} else if (propertyDef == propSourceField) {
			for (SchemaField field : config.getSchema().getFieldList()
					.getList()) {
				if (field.isIndexed())
					values.add(field.getName());
			}
		}
		if (values.size() == 0)
			return null;
		String[] valueArray = new String[values.size()];
		values.toArray(valueArray);
		return valueArray;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propFreqPadSize)
			return "9";
		return null;
	}

	final private static void indexBuffer(List<IndexDocument> buffer,
			Client target) throws SearchLibException, NoSuchAlgorithmException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (buffer.size() == 0)
			return;
		target.updateDocuments(buffer);
		buffer.clear();
	}

	@Override
	public void execute(Client client, TaskProperties properties)
			throws SearchLibException {
		String sourceIndex = properties.getValue(propSourceIndex);
		String sourceField = properties.getValue(propSourceField);
		String login = properties.getValue(propLogin);
		String apiKey = properties.getValue(propApiKey);
		String[] targetTermFields = properties.getValue(propTermField).split(
				",");
		for (int i = 0; i < targetTermFields.length; i++)
			targetTermFields[i] = targetTermFields[i].trim();
		int freqPadSize = Integer
				.parseInt(properties.getValue(propFreqPadSize));
		String[] targetFreqFields = properties.getValue(propFreqField).split(
				",");
		for (int i = 0; i < targetFreqFields.length; i++)
			targetFreqFields[i] = targetFreqFields[i].trim();
		int bufferSize = 50;
		TermEnum termEnum = null;
		try {
			if (!ClientCatalog.getUserList().isEmpty()) {
				User user = ClientCatalog.authenticateKey(login, apiKey);
				if (user == null)
					throw new SearchLibException("Authentication failed");
				if (!user.hasAnyRole(sourceIndex, Role.GROUP_INDEX))
					throw new SearchLibException("Not enough right");
			}
			Client sourceClient = ClientCatalog.getClient(sourceIndex);
			if (sourceClient == null)
				throw new SearchLibException("Client not found: " + sourceIndex);
			SchemaField sourceTermField = sourceClient.getSchema()
					.getFieldList().get(sourceField);
			if (sourceTermField == null)
				throw new SearchLibException("Source field not found: "
						+ sourceField);
			String sourceFieldName = sourceTermField.getName();
			termEnum = sourceClient.getIndex().getTermEnum(sourceFieldName, "");
			Term term = null;
			String text;
			List<IndexDocument> buffer = new ArrayList<IndexDocument>(
					bufferSize);

			while ((term = termEnum.term()) != null) {
				if (!sourceFieldName.equals(term.field()))
					break;
				IndexDocument indexDocument = new IndexDocument();
				text = term.text();
				for (String targetTermField : targetTermFields)
					indexDocument.addString(targetTermField, text);
				text = StringUtils.leftPad(termEnum.docFreq(), freqPadSize);
				for (String targetFreqField : targetFreqFields)
					indexDocument.addString(targetFreqField, text);
				buffer.add(indexDocument);
				if (buffer.size() == bufferSize)
					indexBuffer(buffer, client);
				if (!termEnum.next())
					break;
			}
			indexBuffer(buffer, client);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (NamingException e) {
			throw new SearchLibException(e);
		} finally {
			if (termEnum != null) {
				try {
					termEnum.close();
				} catch (IOException e) {
					Logging.warn(e);
				}
				termEnum = null;
			}
		}
	}
}
