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

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskProperty {

	private Config config;

	private TaskAbstract task;

	private String value;

	private TaskPropertyDef propertyDef;

	protected TaskProperty(Config config, TaskAbstract task,
			TaskPropertyDef propertyDef) {
		this.config = config;
		this.task = task;
		this.propertyDef = propertyDef;
		setValue(task.getDefaultValue(config, propertyDef));
	}

	protected TaskProperty(TaskProperty taskPropSource) {
		this.config = taskPropSource.config;
		this.task = taskPropSource.task;
		this.propertyDef = taskPropSource.propertyDef;
		this.value = taskPropSource.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the type
	 */
	public TaskPropertyType getType() {
		return propertyDef.type;

	}

	/**
	 * @return the cols
	 */
	public int getCols() {
		return propertyDef.cols;
	}

	/**
	 * @return the property name
	 */
	public String getName() {
		return propertyDef.name;
	}

	/**
	 * 
	 * @return the possible value for this property
	 * @throws SearchLibException
	 */
	public String[] getValueList() throws SearchLibException {
		return task.getPropertyValues(config, propertyDef);
	}

	/**
	 * 
	 * @param xmlWriter
	 * @throws SAXException
	 */
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("property", "name", propertyDef.name);
		if (propertyDef.type == TaskPropertyType.password) {
			if (value != null && value.length() > 0)
				xmlWriter.textNode(StringUtils.base64encode(value));
		} else
			xmlWriter.textNode(value);
		xmlWriter.endElement();
	}
}
