/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskItem extends ExecutionAbstract {

	private Config config;

	private TaskAbstract task;

	private TaskProperties userProperties;

	public TaskItem(Config config, TaskAbstract task) {
		this.config = config;
		this.task = task;
		userProperties = new TaskProperties(config, task,
				task.getPropertyList());
	}

	public TaskItem(TaskItem selectedJobTask) {
		this(selectedJobTask.config, selectedJobTask.task);
		setProperties(selectedJobTask.getProperties());
	}

	public void setProperties(TaskProperty[] taskProps) {
		if (taskProps == null)
			return;
		for (TaskProperty taskProperty : taskProps)
			userProperties.set(new TaskProperty(taskProperty));
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
	public TaskProperty[] getProperties() {
		return userProperties.getArray();
	}

	public void run(Client client, TaskLog taskLog) throws SearchLibException,
			IOException {
		setRunningNow();
		try {
			task.execute(client, userProperties, taskLog);
		} finally {
			runningEnd();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		xmlWriter
				.startElement("task", "class", task.getClass().getSimpleName());
		userProperties.writeXml(xmlWriter);
		xmlWriter.endElement();
	}

	public static TaskItem fromXml(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException {
		String taskClass = XPathParser.getAttributeString(node, "class");
		if (taskClass == null)
			return null;
		TaskAbstract taskAbstract = config.getJobTaskEnum()
				.findClass(taskClass);
		if (taskAbstract == null)
			return null;
		TaskItem taskItem = new TaskItem(config, taskAbstract);
		NodeList nodeList = xpp.getNodeList(node, "property");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node propNode = nodeList.item(i);
			String name = XPathParser.getAttributeString(propNode, "name");
			String value = xpp.getNodeString(propNode, false);
			TaskPropertyDef propDef = taskAbstract.findProperty(name);
			if (propDef != null) {
				if (propDef.type == TaskPropertyType.password)
					value = StringUtils.base64decode(value);
				taskItem.userProperties.setValue(propDef, value);
			}
		}
		return taskItem;
	}

}
