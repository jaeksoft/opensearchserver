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

import java.util.Properties;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskItem {

	private TaskAbstract task;

	private Properties properties;

	public TaskItem(TaskAbstract task) {
		this.task = task;
		properties = new Properties();
	}

	/**
	 * @return the task
	 */
	public TaskAbstract getTask() {
		return task;
	}

	/**
	 * 
	 * @param name
	 * @return the property value
	 */
	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	/**
	 * Set the property
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter
				.startElement("task", "class", task.getClass().getSimpleName());
		String[] propertyList = task.getPropertyList();
		if (propertyList != null) {
			for (String propertyName : propertyList) {
				String value = properties.getProperty(propertyName);
				if (value != null) {
					xmlWriter.startElement("property", "name", propertyName);
					xmlWriter.textNode(value);
					xmlWriter.endElement();
				}
			}
		}
		xmlWriter.endElement();
	}
}
