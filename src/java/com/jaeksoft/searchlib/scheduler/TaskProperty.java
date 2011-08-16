/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskProperty {

	private Config config;

	private TaskAbstract task;

	private String name;

	private String value;

	protected TaskProperty(Config config, TaskAbstract task, String propertyName) {
		this.config = config;
		this.task = task;
		this.name = propertyName;
		setValue(null);
	}

	protected TaskProperty(TaskProperty taskPropSource) {
		this.config = taskPropSource.config;
		this.task = taskPropSource.task;
		this.name = taskPropSource.name;
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
	 * @return the property name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the possible value for this property
	 * @throws SearchLibException
	 */
	public String[] getValueList() throws SearchLibException {
		return task.getPropertyValues(config, name);
	}

	/**
	 * 
	 * @param xmlWriter
	 * @throws SAXException
	 */
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("property", "name", name);
		xmlWriter.textNode(value);
		xmlWriter.endElement();
	}
}
