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

import java.util.Map;
import java.util.TreeMap;

public class Variables {

	public Map<String, String> map;

	public Variables() {
		map = null;
	}

	public Variables(Map<String, String> variables) {
		put(variables);
	}

	private final void checkMap() {
		if (map == null)
			map = new TreeMap<String, String>();
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
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		sb.append(name);
		sb.append('}');
		map.put(sb.toString(), value);
	}

	public final String replace(String text) {
		if (map == null || text == null)
			return text;
		for (Map.Entry<String, String> entry : map.entrySet())
			text = text.replace(entry.getKey(), entry.getValue());
		return text;
	}
}
