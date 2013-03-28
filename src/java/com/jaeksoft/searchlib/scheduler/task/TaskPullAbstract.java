/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;

public abstract class TaskPullAbstract extends TaskAbstract {

	final protected TaskPropertyDef propSourceIndex = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Index source", "Index source",
			"The source index which will be read", 100);

	final protected TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login (Index target)",
			"Login (Index target)",
			"The login used to connect to the targeted index", 20);

	final protected TaskPropertyDef propApiKey = new TaskPropertyDef(
			TaskPropertyType.password, "API Key (Index target)",
			"API Key (Index target)",
			"The API key used to connect to the targeted index", 50);

	final protected TaskPropertyDef propSourceField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source field name", "Source field name",
			null, 50);

	final protected TaskPropertyDef propTargetField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Target field name", "Target field name",
			null, 50);

	final protected TaskPropertyDef propTargetMappedFields = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "Mapped fields on target",
			"Mapped fields on target", null, 80, 5);

	final protected TaskPropertyDef propBufferSize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Buffer size", "Buffer size", null, 10);

	final protected TaskPropertyDef propLanguage = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Language", "Language", null, 30);

	protected void populateSourceIndexValues(Config config, List<String> values)
			throws SearchLibException {
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null)) {
			String v = item.getIndexName();
			if (!v.equals(config.getIndexName()))
				values.add(v);
		}
	}

	protected void populateFieldValues(Config config, List<String> values) {
		config.getSchema().getFieldList().getIndexedFields(values);
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		List<String> values = new ArrayList<String>(0);
		if (propertyDef == propSourceIndex) {
			populateSourceIndexValues(config, values);
		} else if (propertyDef == propLanguage) {
			return LanguageEnum.stringArray();
		} else if (propertyDef == propTargetField) {
			populateFieldValues(config, values);
		} else if (propertyDef == propSourceField) {
			populateFieldValues(config, values);
		}
		return StringUtils.toStringArray(values, false);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propBufferSize)
			return "50";
		else if (propertyDef == propLanguage)
			return LanguageEnum.UNDEFINED.getName();
		return null;
	}

	protected class ExecutionData {

		private final FieldMap targetFieldMap;

		protected final int bufferSize;

		private final List<IndexDocument> buffer;

		private final String targetField;

		protected final String sourceField;

		protected final Client sourceClient;

		protected final LanguageEnum lang;

		private int totalCount;

		protected ExecutionData(TaskProperties properties, Client client)
				throws IOException, SearchLibException, NamingException {
			String sourceIndex = properties.getValue(propSourceIndex);
			sourceField = properties.getValue(propSourceField);

			lang = LanguageEnum.findByName(properties.getValue(propLanguage));
			String targetMappedFields = properties
					.getValue(propTargetMappedFields);
			targetField = properties.getValue(propTargetField);
			bufferSize = Integer.parseInt(properties.getValue(propBufferSize));
			targetFieldMap = new FieldMap(targetMappedFields, ',', '|');
			targetFieldMap.cacheAnalyzers(client.getSchema().getAnalyzerList(),
					lang);
			buffer = new ArrayList<IndexDocument>(bufferSize);
			totalCount = 0;
			String login = properties.getValue(propLogin);
			String apiKey = properties.getValue(propApiKey);

			if (!ClientCatalog.getUserList().isEmpty()) {
				User user = ClientCatalog.authenticateKey(login, apiKey);
				if (user == null)
					throw new SearchLibException("Authentication failed");
				if (!user.hasAnyRole(sourceIndex, Role.GROUP_INDEX))
					throw new SearchLibException("Not enough right");
			}
			sourceClient = ClientCatalog.getClient(sourceIndex);
			if (sourceClient == null)
				throw new SearchLibException("Client not found: " + sourceIndex);

			SchemaField sourceTermField = sourceClient.getSchema()
					.getFieldList().get(sourceField);
			if (sourceTermField == null)
				throw new SearchLibException("Source field not found: "
						+ sourceField);
		}

		final protected void indexBuffer(Client target, TaskLog taskLog)
				throws SearchLibException, NoSuchAlgorithmException,
				IOException, URISyntaxException, InstantiationException,
				IllegalAccessException, ClassNotFoundException {
			if (buffer.size() == 0)
				return;
			totalCount += target.updateDocuments(buffer);
			buffer.clear();
			taskLog.setInfo(totalCount + " document(s) indexed");
			if (taskLog.isAbortRequested())
				throw new SearchLibException.AbortException();

		}

		final protected void indexDocument(Client target,
				IndexDocument mappedDocument, String value, TaskLog taskLog)
				throws IOException, NoSuchAlgorithmException,
				SearchLibException, URISyntaxException, InstantiationException,
				IllegalAccessException, ClassNotFoundException {
			IndexDocument targetDocument = new IndexDocument(mappedDocument);
			targetDocument.add(targetField, value, null);
			IndexDocument finalDocument = new IndexDocument(targetDocument);
			targetFieldMap.mapIndexDocument(targetDocument, finalDocument);
			buffer.add(finalDocument);
			if (buffer.size() == bufferSize)
				indexBuffer(target, taskLog);
		}
	}
}
