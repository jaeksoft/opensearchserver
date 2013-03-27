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

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Client;
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
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class TaskPullFields extends TaskPullAbstract {

	final private TaskPropertyDef propSourceQuery = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source query", 50);

	final private TaskPropertyDef propSourceMappedFields = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "Mapped fields on source", 80, 5);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceIndex,
			propLogin, propApiKey, propSourceQuery, propLanguage,
			propSourceField, propTargetField, propSourceMappedFields,
			propTargetMappedFields, propBufferSize };

	@Override
	public String getName() {
		return "Pull fields";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propSourceQuery)
			return "*:*";
		return super.getDefaultValue(config, propertyDef);
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String sourceQuery = properties.getValue(propSourceQuery);
		String sourceMappedFields = properties.getValue(propSourceMappedFields);

		try {

			ExecutionData executionData = new ExecutionData(properties, client);

			FieldMap sourceFieldMap = new FieldMap(sourceMappedFields, ',', '|');
			sourceFieldMap.cacheAnalyzers(client.getSchema().getAnalyzerList(),
					LanguageEnum.UNDEFINED);

			SearchRequest searchRequest = new SearchRequest(
					executionData.sourceClient);
			searchRequest.setQueryString(sourceQuery);
			searchRequest.addReturnField(executionData.sourceField);
			for (GenericLink<SourceField, TargetField> link : sourceFieldMap
					.getList())
				link.getSource().addReturnField(searchRequest);
			searchRequest.setRows(executionData.bufferSize);
			int start = 0;

			for (;;) {
				searchRequest.setStart(start);
				AbstractResultSearch result = (AbstractResultSearch) executionData.sourceClient
						.request(searchRequest);

				if (result.getDocumentCount() <= 0)
					break;

				for (ResultDocument document : result) {
					FieldValueItem[] fieldValueItems = document
							.getValueArray(executionData.sourceField);
					if (fieldValueItems == null)
						continue;

					IndexDocument mappedDocument = new IndexDocument(
							executionData.lang);
					sourceFieldMap.mapIndexDocument(document, mappedDocument);

					for (FieldValueItem fieldValueItem : fieldValueItems) {

						String value = fieldValueItem.getValue();
						if (value == null)
							continue;
						if (value.length() == 0)
							continue;

						executionData.indexDocument(client, mappedDocument,
								value, taskLog);

					}
				}

				searchRequest.reset();
				start += executionData.bufferSize;
			}
			executionData.indexBuffer(client, taskLog);
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
