/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.StringUtils;

public class TaskPullTerms extends TaskPullAbstract {

	final private TaskPropertyDef propSourceField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Source field name", 50);

	final private TaskPropertyDef propTermField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Term field name", 50);

	final private TaskPropertyDef propFreqField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Frequency field name", 50);

	final private TaskPropertyDef propFreqMin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Minimum frequency", 20);

	final private TaskPropertyDef propFreqPadSize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Frequency pad", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceField,
			propSourceIndex, propLogin, propApiKey, propTermField,
			propFreqField, propFreqMin, propFreqPadSize };

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
			populateSourceIndexValues(config, values);
		} else if (propertyDef == propSourceField) {
			populateFieldValues(config, values);
		}
		return toValueArray(values);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propFreqPadSize)
			return "9";
		if (propertyDef == propFreqMin)
			return "2";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		String sourceIndex = properties.getValue(propSourceIndex);
		String sourceField = properties.getValue(propSourceField);
		String login = properties.getValue(propLogin);
		String apiKey = properties.getValue(propApiKey);
		String[] targetTermFields = StringUtils.split(
				properties.getValue(propTermField), ',');
		for (int i = 0; i < targetTermFields.length; i++)
			targetTermFields[i] = targetTermFields[i].trim();
		int freqPadSize = Integer
				.parseInt(properties.getValue(propFreqPadSize));
		int freqMin = Integer.parseInt(properties.getValue(propFreqMin));
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

			int totalCount = 0;
			while ((term = termEnum.term()) != null) {
				if (!sourceFieldName.equals(term.field()))
					break;
				IndexDocument indexDocument = new IndexDocument();
				text = term.text();
				int freq = termEnum.docFreq();
				if (freq >= freqMin) {
					for (String targetTermField : targetTermFields)
						indexDocument.addString(targetTermField, text);
					text = StringUtils.leftPad(freq, freqPadSize);
					for (String targetFreqField : targetFreqFields)
						indexDocument.addString(targetFreqField, text);
					buffer.add(indexDocument);
					if (buffer.size() == bufferSize)
						totalCount = indexBuffer(totalCount, buffer, client,
								taskLog);
				}
				if (!termEnum.next())
					break;
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
