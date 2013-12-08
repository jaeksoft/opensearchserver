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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.Variables;
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
			TaskPropertyType.textBox, "JSON Path request", "JsonPathRequest",
			"The JSON Path query to apply to the result", 70);

	final private TaskPropertyDef propJsonResultComparator = new TaskPropertyDef(
			TaskPropertyType.listBox, "JSON result comparator",
			"JsonResultComparator", "The JSON comparator", 70);

	final private static String COMPARATOR_GREATER = ">";
	final private static String COMPARATOR_GREATER_OR_EQUAL = ">=";
	final private static String COMPARATOR_LESSER = "<";
	final private static String COMPARATOR_LESSER_OR_EQUAL = "<=";
	final private static String COMPARATOR_EQUAL = "=";

	final private static String[] RESULT_COMPARATORS = { COMPARATOR_LESSER,
			COMPARATOR_LESSER_OR_EQUAL, COMPARATOR_EQUAL,
			COMPARATOR_GREATER_OR_EQUAL, COMPARATOR_GREATER };

	final private TaskPropertyDef propJsonResultValue = new TaskPropertyDef(
			TaskPropertyType.textBox, "JSON result value", "JsonResultValue",
			"The expected JSON value", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSearchTemplate,
			propQueryString, propJsonPath, propJsonResultComparator,
			propJsonResultValue };

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
		List<String> nameList = new ArrayList<String>(0);
		if (propJsonResultComparator == propertyDef)
			return RESULT_COMPARATORS;
		if (propSearchTemplate == propertyDef)
			config.getRequestMap().getNameList(nameList,
					RequestTypeEnum.SearchFieldRequest,
					RequestTypeEnum.SearchRequest);
		if (nameList.size() == 0)
			return null;
		return nameList.toArray(new String[nameList.size()]);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propJsonResultComparator == propertyDef)
			return COMPARATOR_EQUAL;
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {
		String searchTemplate = properties.getValue(propSearchTemplate);
		String queryString = properties.getValue(propQueryString);
		String jsonPath = properties.getValue(propJsonPath);
		String resultComparator = properties.getValue(propJsonResultComparator);
		String resultValue = properties.getValue(propJsonResultValue);
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
				JsonPath jsonPathCompile = JsonPath.compile(jsonPath);
				Object jsonPathResult = jsonPathCompile.read((String) json);
				if (jsonPathResult == null)
					throw new SearchLibException("The JSON Path query failed");
				if (jsonPathResult instanceof Integer)
					compareResultNumber((Integer) jsonPathResult,
							resultComparator, Integer.parseInt(resultValue));
				else if (jsonPathResult instanceof Double)
					compareResultNumber((Double) jsonPathResult,
							resultComparator, Double.parseDouble(resultValue));
				if (jsonPathResult instanceof Float)
					compareResultNumber((Float) jsonPathResult,
							resultComparator, Float.parseFloat(resultValue));
				else
					compareResultNumber(jsonPathResult.toString(),
							resultComparator, resultValue);
				taskLog.setInfo("JSON Path succeed: " + jsonPathResult);
			}
		} catch (JsonProcessingException e) {
			throw new SearchLibException(e);
		}
	}

	private <T extends Comparable<T>> void compareResultNumber(
			T jsonPathResult, String resultComparator, T resultValue)
			throws SearchLibException {
		if (resultComparator.equals(COMPARATOR_EQUAL))
			if (jsonPathResult.equals(resultValue))
				return;
		if (resultComparator.equals(COMPARATOR_LESSER))
			if (jsonPathResult.compareTo(resultValue) < 0)
				return;
		if (resultComparator.equals(COMPARATOR_LESSER_OR_EQUAL))
			if (jsonPathResult.compareTo(resultValue) <= 0)
				return;
		if (resultComparator.equals(COMPARATOR_GREATER))
			if (jsonPathResult.compareTo(resultValue) > 0)
				return;
		if (resultComparator.equals(COMPARATOR_GREATER_OR_EQUAL))
			if (jsonPathResult.compareTo(resultValue) >= 0)
				return;
		throw new SearchLibException("Wrong returned value: " + jsonPathResult
				+ ". Expected: " + resultComparator + ' ' + resultValue);
	}
}
