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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskItem {

	private TaskAbstract task;

	private List<TaskProperty> userProperties;

	public TaskItem(Config config, TaskAbstract task) {
		this.task = task;
		userProperties = new ArrayList<TaskProperty>();
		String[] propertyNames = task.getPropertyList();
		if (propertyNames != null) {
			for (String propertyName : propertyNames)
				userProperties
						.add(new TaskProperty(config, task, propertyName));
		}
	}

	/**
	 * @return the task
	 */
	public TaskAbstract getTask() {
		return task;
	}

	/**
	 * 
	 * @return the property list
	 */
	public List<TaskProperty> getProperties() {
		return userProperties;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter
				.startElement("task", "class", task.getClass().getSimpleName());
		for (TaskProperty taskProperty : userProperties)
			taskProperty.writeXml(xmlWriter);
		xmlWriter.endElement();
	}

	public static TaskItem fromXml(Config config, Node node)
			throws XPathExpressionException {
		String taskClass = XPathParser.getAttributeString(node, "class");
		if (taskClass == null)
			return null;
		TaskAbstract taskAbstract = TaskEnum.findClass(taskClass);
		if (taskAbstract == null)
			return null;
		return new TaskItem(config, taskAbstract);
	}
}
