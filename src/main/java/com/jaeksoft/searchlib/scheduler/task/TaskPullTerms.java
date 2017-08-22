/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.scheduler.task;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.Variables;
import org.apache.lucene.index.Term;

import javax.naming.NamingException;
import java.io.IOException;

public class TaskPullTerms extends TaskPullAbstract {

	final private TaskPropertyDef propFreqField =
			new TaskPropertyDef(TaskPropertyType.textBox, "Frequency field name", "Frequency field name", null, 50);

	final private TaskPropertyDef propFreqMin =
			new TaskPropertyDef(TaskPropertyType.textBox, "Minimum frequency", "Minimum frequency", null, 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceIndex,
			propLogin,
			propApiKey,
			propSourceField,
			propTargetField,
			propFreqField,
			propFreqMin,
			propLanguage,
			propTargetMappedFields,
			propBufferSize };

	@Override
	public String getName() {
		return "Pull terms";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propFreqMin)
			return "2";
		return super.getDefaultValue(config, propertyDef);
	}

	@Override
	public void execute(Client client, TaskProperties properties, Variables variables, TaskLog taskLog)
			throws SearchLibException {
		int freqMin = Integer.parseInt(properties.getValue(propFreqMin));
		final String[] targetFreqFields;
		String freqField = properties.getValue(propFreqField);
		if (freqField != null && freqField.length() > 0)
			targetFreqFields = freqField.split(",");
		else
			targetFreqFields = null;
		if (targetFreqFields != null)
			for (int i = 0; i < targetFreqFields.length; i++)
				targetFreqFields[i] = targetFreqFields[i].trim();
		try {
			final ExecutionData executionData = new ExecutionData(properties, client);
			executionData.sourceClient.termEnum(new Term(executionData.sourceField, ""), termEnum -> {
				Term term;
				while ((term = termEnum.term()) != null) {
					if (!executionData.sourceField.equals(term.field()))
						break;
					int freq = termEnum.docFreq();
					if (freq >= freqMin) {
						String termText = term.text();
						String freqText = Integer.toString(freq);
						IndexDocument indexDocument = new IndexDocument(executionData.lang);
						if (targetFreqFields != null)
							for (String targetFreqField : targetFreqFields)
								indexDocument.addString(targetFreqField, freqText);
						executionData.indexDocument(client, indexDocument, termText, taskLog);
					}
					if (!termEnum.next())
						break;
				}
			});

			executionData.indexBuffer(client, taskLog);
		} catch (IOException | NamingException e) {
			throw new SearchLibException(e);
		}
	}
}
