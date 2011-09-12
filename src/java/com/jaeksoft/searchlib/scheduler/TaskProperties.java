/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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
			TaskPropertyDef[] propertyDefs) {
		map = new LinkedHashMap<String, TaskProperty>();
		if (propertyDefs == null)
			return;
		cache = new TaskProperty[propertyDefs.length];
		int i = 0;
		for (TaskPropertyDef propertyDef : propertyDefs) {
			TaskProperty taskProperty = new TaskProperty(config, task,
					propertyDef);
			map.put(propertyDef.name, taskProperty);
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
