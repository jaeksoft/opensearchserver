/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.utils.Variables;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;
import com.jayway.jsonpath.JsonPath;

public class TaskQueryCheck extends TaskAbstract {

	final private TaskPropertyDef propSearchTemplate = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Search template", "SearchTemplate",
			"The search query to use", 50);

	final private TaskPropertyDef propQueryString = new TaskPropertyDef(
			TaskPropertyType.textBox, "Query string", "QueryString",
			"The query string to pass to the search template", 50);

	final private TaskPropertyDef propJsonPath = new TaskPropertyDef(
			TaskPropertyType.textBox, "Json Path request", "JsonPathRequest",
			"The JSON Path query to apply to the result", 100);

	final private TaskPropertyDef propXPath = new TaskPropertyDef(
			TaskPropertyType.textBox, "XPath request", "XPathRequest",
			"The XPath query to apply to the result", 100);

	final private TaskPropertyDef[] taskPropertyDefs = { propSearchTemplate,
			propQueryString, propJsonPath };

	@Override
	public String getName() {
		return "Query check";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {
		String searchTemplate = properties.getValue(propSearchTemplate);
		String queryString = properties.getValue(propQueryString);
		String jsonPath = properties.getValue(propJsonPath);
		taskLog.setInfo("Query check");
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) client
				.getNewRequest(searchTemplate);
		if (searchRequest == null)
			throw new SearchLibException("Request template  " + searchTemplate
					+ " not found");
		searchRequest.setQueryString(queryString);
		try {
			taskLog.setInfo("Execute request " + searchTemplate);
			SearchResult searchResult = new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
			if (jsonPath != null && jsonPath.length() > 0) {
				String json = JsonUtils.toJsonString(searchResult);
				System.out.println(jsonPath);
				System.out.println(json);
				Object jsonPathResult = JsonPath.read(json, jsonPath);
				if (jsonPathResult == null)
					throw new SearchLibException("The JSON Path query failed");
				taskLog.setInfo("JSON Path succeed: " + jsonPathResult);
			}
		} catch (JsonProcessingException e) {
			throw new SearchLibException(e);
		}
	}
}
