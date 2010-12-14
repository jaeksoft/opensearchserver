/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.scheduler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskProperties {

	private Map<String, TaskProperty> map;

	private TaskProperty[] cache;

	public TaskProperties(Config config, TaskAbstract task,
			String[] propertyNames) {
		map = new LinkedHashMap<String, TaskProperty>();
		if (propertyNames == null)
			return;
		cache = new TaskProperty[propertyNames.length];
		int i = 0;
		for (String propertyName : propertyNames) {
			TaskProperty taskProperty = new TaskProperty(config, task,
					propertyName);
			map.put(propertyName, taskProperty);
			cache[i++] = taskProperty;
		}
	}

	public TaskProperty[] getArray() {
		return cache;
	}

	public String getValue(String propertyName) {
		TaskProperty prop = map.get(propertyName);
		if (prop == null)
			return null;
		return prop.getValue();
	}

	public void setValue(String propertyName, String value) {
		TaskProperty prop = map.get(propertyName);
		if (prop == null)
			return;
		prop.setValue(value);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		if (cache != null)
			for (TaskProperty taskProperty : cache)
				taskProperty.writeXml(xmlWriter);
	}

}
