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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.UniqueNameItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JobItem extends UniqueNameItem<JobItem> {

	protected final static String JOB_NODE_NAME = "job";

	private ReadWriteLock rwl = new ReadWriteLock();

	private TaskCronExpression cron;

	private List<TaskItem> tasks;

	private boolean active;

	public JobItem(String name) {
		super(name);
		tasks = new ArrayList<TaskItem>();
		cron = new TaskCronExpression();
	}

	public void copy(JobItem job) {
		rwl.w.lock();
		try {
			job.rwl.r.lock();
			try {
				setName(job.getName());
				active = job.active;
				tasks.clear();
				for (TaskItem task : job.tasks)
					tasks.add(task);
				cron.copy(job.getCron());
			} finally {
				job.rwl.r.unlock();
			}
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the cron
	 */
	public TaskCronExpression getCron() {
		rwl.r.lock();
		try {
			return cron;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @return the task list
	 */
	public List<TaskItem> getTasks() {
		rwl.r.lock();
		try {
			return tasks;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Add a task to the list
	 * 
	 * @param task
	 */
	public void taskAdd(TaskItem task) {
		rwl.w.lock();
		try {
			tasks.add(task);
		} finally {
			rwl.w.unlock();
		}
	}

	public void taskUp(TaskItem task) {
		rwl.w.lock();
		try {
			int i = tasks.indexOf(task);
			if (i == -1 || i == 0)
				return;
			tasks.remove(i);
			tasks.add(i - 1, task);
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Move a task down
	 * 
	 * @param filter
	 */
	public void taskDown(TaskItem task) {
		rwl.w.lock();
		try {

			int i = tasks.indexOf(task);
			if (i == -1 || i == tasks.size() - 1)
				return;
			tasks.remove(i);
			tasks.add(i + 1, task);
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Remove the filter
	 * 
	 * @param filter
	 */
	public void taskRemove(TaskItem task) {
		rwl.w.lock();
		try {
			tasks.remove(task);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("job", "name", this.getName(), "active",
					active ? "yes" : "no");
			cron.writeXml(xmlWriter);
			for (TaskItem task : tasks)
				task.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public static JobItem fromXml(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException {
		String name = XPathParser.getAttributeString(node, "name");
		boolean active = "yes".equalsIgnoreCase(XPathParser.getAttributeString(
				node, "active"));
		if (name == null)
			return null;
		JobItem jobItem = new JobItem(name);
		jobItem.setActive(active);
		NodeList tasks = xpp.getNodeList(node, "task");
		for (int i = 0; i < tasks.getLength(); i++) {
			TaskItem taskItem = TaskItem.fromXml(config, tasks.item(i));
			if (taskItem != null)
				jobItem.taskAdd(taskItem);
		}
		return jobItem;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		rwl.w.lock();
		try {
			this.active = active;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		rwl.r.lock();
		try {
			return active;
		} finally {
			rwl.r.unlock();
		}
	}

}
