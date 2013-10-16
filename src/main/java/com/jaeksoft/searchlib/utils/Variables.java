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

package com.jaeksoft.searchlib.utils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class Variables {

	public Map<String, String> map;

	public Variables() {
		map = null;
	}

	public Variables(Map<String, String> variables) {
		put(variables);
	}

	public Variables(Variables vars) {
		if (vars == null)
			return;
		put(vars.map);
	}

	public Variables(String json) throws JsonParseException,
			JsonMappingException, IOException {
		Map<String, String> jsonMap = JsonUtils.getObject(json,
				new TypeReference<Map<String, String>>() {
				});
		for (Map.Entry<String, String> entry : jsonMap.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	private final void checkMap() {
		if (map == null)
			map = new TreeMap<String, String>();
	}

	public final void put(Variables variables) {
		if (variables == null)
			return;
		put(variables.map);
	}

	public final void put(Map<String, String> variables) {
		if (variables == null || variables.size() == 0)
			return;
		checkMap();
		for (Map.Entry<String, String> entry : variables.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	public final void put(String name, String value) {
		if (name == null || value == null)
			return;
		checkMap();
		map.put(StringUtils.fastConcat("{", name, "}"), value);
	}

	public final String replace(String text) {
		if (map == null || text == null)
			return text;
		for (Map.Entry<String, String> entry : map.entrySet())
			text = text.replace(entry.getKey(), entry.getValue());
		return text;
	}
}
