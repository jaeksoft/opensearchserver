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
package com.jaeksoft.searchlib.crawler.rest;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.database.DatabaseFieldMap;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;

public class RestFieldMap extends DatabaseFieldMap {

	public void mapJson(FieldMapContext context, Object jsonObject, IndexDocument target)
			throws SearchLibException, IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			String jsonPath = link.getSource().getUniqueName();
			try {
				Object jsonContent = JsonPath.read(jsonObject, jsonPath);
				if (jsonContent == null)
					continue;
				if (jsonContent instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) jsonContent;
					for (Object content : jsonArray) {
						if (content != null)
							mapFieldTarget(context, link.getTarget(), false, content.toString(), target, null);
					}
				} else
					mapFieldTarget(context, link.getTarget(), false, jsonContent.toString(), target, null);
			} catch (PathNotFoundException | IllegalArgumentException e) {
				Logging.warn(e);
			}
		}
	}
}
