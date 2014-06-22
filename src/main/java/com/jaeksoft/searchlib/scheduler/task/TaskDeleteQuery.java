/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

<<<<<<< HEAD
import java.io.IOException;
=======
>>>>>>> dbd8701... Implements #710
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskDeleteQuery extends TaskAbstract {

	final private TaskPropertyDef propQuery = new TaskPropertyDef(
			TaskPropertyType.textBox, "Query", "Query",
			"The search query which returns the documents to delete", 200);

	final private TaskPropertyDef propTemplate = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Template", "Template",
			"The search template which returns the documents to delete", 50);

	final private TaskPropertyDef[] taskPropertyDefs = { propQuery,
			propTemplate };

	@Override
	public String getName() {
		return "Delete query";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		List<String> nameList = new ArrayList<String>();
		if (propTemplate == propertyDef)
			config.getRequestMap().getNameList(nameList,
					RequestTypeEnum.SearchFieldRequest,
					RequestTypeEnum.SearchRequest);
		if (nameList.size() == 0)
			return null;
		return nameList.toArray(new String[nameList.size()]);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException {
		String query = properties.getValue(propQuery);
		String template = properties.getValue(propTemplate);
		boolean bQuery = !StringUtils.isEmpty(query);
		boolean bTemplate = !StringUtils.isEmpty(template);
		AbstractSearchRequest request = null;
		if (bQuery) {
			request = new SearchPatternRequest(client);
			request.setQueryString(query);
		} else if (bTemplate) {
			AbstractRequest abstractRequest = client.getNewRequest(template);
			if (abstractRequest == null)
				throw new SearchLibException("Search template not found: "
						+ template);
			if (!((abstractRequest instanceof AbstractSearchRequest)))
				throw new SearchLibException("Wrong template type: " + template);
			request = (AbstractSearchRequest) abstractRequest;
		}
		taskLog.setInfo("Deletion request");
		int i = client.deleteDocuments(request);
		taskLog.setInfo(i + " document(s) deleted");
	}
}
