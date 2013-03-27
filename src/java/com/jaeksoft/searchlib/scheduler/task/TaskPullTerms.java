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

import javax.naming.NamingException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskPullTerms extends TaskPullAbstract {

	final private TaskPropertyDef propFreqField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Frequency field name", 50);

	final private TaskPropertyDef propFreqMin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Minimum frequency", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSourceIndex,
			propLogin, propApiKey, propSourceField, propTargetField,
			propFreqField, propFreqMin, propLanguage, propTargetMappedFields,
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
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException {
		int freqMin = Integer.parseInt(properties.getValue(propFreqMin));
		String[] targetFreqFields = null;
		String freqField = properties.getValue(propFreqField);
		if (freqField != null && freqField.length() > 0)
			targetFreqFields = freqField.split(",");
		if (targetFreqFields != null)
			for (int i = 0; i < targetFreqFields.length; i++)
				targetFreqFields[i] = targetFreqFields[i].trim();
		TermEnum termEnum = null;
		try {
			ExecutionData executionData = new ExecutionData(properties, client);

			termEnum = executionData.sourceClient.getTermEnum(
					executionData.sourceField, "");
			Term term = null;

			while ((term = termEnum.term()) != null) {
				if (!executionData.sourceField.equals(term.field()))
					break;
				int freq = termEnum.docFreq();
				if (freq >= freqMin) {
					String termText = term.text();
					String freqText = Integer.toString(freq);
					IndexDocument indexDocument = new IndexDocument(
							executionData.lang);
					if (targetFreqFields != null)
						for (String targetFreqField : targetFreqFields)
							indexDocument.addString(targetFreqField, freqText);
					executionData.indexDocument(client, indexDocument,
							termText, taskLog);
				}
				if (!termEnum.next())
					break;
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
