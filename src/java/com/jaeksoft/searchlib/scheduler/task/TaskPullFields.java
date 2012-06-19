/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class TaskPullFields extends TaskPullAbstract {

	final private TaskPropertyDef propSourceQuery = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source query", 50);

	final private TaskPropertyDef propSourceField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source field name", 50);

	final private TaskPropertyDef propTargetField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Target field name", 50);

	final private TaskPropertyDef propSourceMappedFields = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "Mapped fields on source", 80, 5);

	final private TaskPropertyDef propTargetMappedFields = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "Mapped fields on target", 80, 5);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceIndex,
			propLogin, propApiKey, propSourceQuery, propSourceField,
			propTargetField, propSourceMappedFields, propTargetMappedFields };

	@Override
	public String getName() {
		return "Pull fields";
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
			populateSourceIndexValues(config, values);
		} else if (propertyDef == propTargetField) {
			populateFieldValues(config, values);
		}
		return toValueArray(values);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propSourceQuery)
			return "*:*";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String sourceIndex = properties.getValue(propSourceIndex);
		String sourceQuery = properties.getValue(propSourceQuery);
		String sourceField = properties.getValue(propSourceField);
		String targetField = properties.getValue(propTargetField);
		String sourceMappedFields = properties.getValue(propSourceMappedFields);
		String targetMappedFields = properties.getValue(propTargetMappedFields);
		String login = properties.getValue(propLogin);
		String apiKey = properties.getValue(propApiKey);

		int bufferSize = 50;

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

			FieldMap sourceFieldMap = new FieldMap(sourceMappedFields, ',', '|');
			sourceFieldMap.cacheAnalyzers(client.getSchema().getAnalyzerList(),
					LanguageEnum.UNDEFINED);

			FieldMap targetFieldMap = new FieldMap(targetMappedFields, ',', '|');
			targetFieldMap.cacheAnalyzers(client.getSchema().getAnalyzerList(),
					LanguageEnum.UNDEFINED);

			List<IndexDocument> buffer = new ArrayList<IndexDocument>(
					bufferSize);

			SearchRequest searchRequest = new SearchRequest(sourceClient);
			searchRequest.setQueryString(sourceQuery);
			searchRequest.addReturnField(sourceField);
			for (GenericLink<SourceField, TargetField> link : sourceFieldMap
					.getList())
				link.getSource().addReturnField(searchRequest);
			searchRequest.setRows(bufferSize);
			int start = 0;

			int totalCount = 0;
			for (;;) {
				searchRequest.setStart(start);
				AbstractResultSearch result = (AbstractResultSearch) sourceClient
						.request(searchRequest);

				if (result.getDocumentCount() <= 0)
					break;

				ResultDocument[] documents = result.getDocuments();
				if (documents == null)
					break;

				for (ResultDocument document : documents) {
					FieldValueItem[] fieldValueItems = document
							.getValueArray(sourceField);
					if (fieldValueItems == null)
						continue;

					IndexDocument mappedDocument = new IndexDocument();
					sourceFieldMap.mapIndexDocument(document, mappedDocument);

					for (FieldValueItem fieldValueItem : fieldValueItems) {

						String value = fieldValueItem.getValue();
						if (value == null)
							continue;
						if (value.length() == 0)
							continue;

						IndexDocument targetDocument = new IndexDocument(
								mappedDocument);
						targetDocument.add(targetField, value, null);
						IndexDocument finalDocument = new IndexDocument(
								targetDocument);
						targetFieldMap.mapIndexDocument(targetDocument,
								finalDocument);

						buffer.add(finalDocument);
						if (buffer.size() == bufferSize)
							totalCount = indexBuffer(totalCount, buffer,
									client, taskLog);
					}
				}

				searchRequest.reset();
				start += bufferSize;
			}
			totalCount = indexBuffer(totalCount, buffer, client, taskLog);
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
		}
	}
}
