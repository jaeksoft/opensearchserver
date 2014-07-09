/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.mail.EmailException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.config.Mailer;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JobItem extends ExecutionAbstract {

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected final static String JOB_NODE_NAME = "job";

	private String name;

	private TaskCronExpression cron;

	private List<TaskItem> tasks;

	private SearchLibException lastError;

	private JobLog jobLog;

	private TaskLog currentTaskLog;

	private boolean emailNotificationOnFailure;

	private String emailRecipients;

	public JobItem(String name) {
		this.name = name;
		tasks = new ArrayList<TaskItem>();
		cron = new TaskCronExpression();
		jobLog = new JobLog(200);
		setLastError(null);
	}

	public void copyFrom(JobItem job) {
		rwl.w.lock();
		try {
			job.rwl.r.lock();
			try {
				this.name = job.name;
				this.emailNotificationOnFailure = job.emailNotificationOnFailure;
				this.emailRecipients = job.emailRecipients;
				this.setActive(job.isActive());
				tasks.clear();
				for (TaskItem task : job.tasks)
					tasks.add(new TaskItem(task));
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

	protected List<TaskItem> getTaskListCopy() {
		rwl.r.lock();
		try {
			List<TaskItem> list = new ArrayList<TaskItem>(0);
			for (TaskItem task : tasks)
				list.add(new TaskItem(task));
			return list;
		} finally {
			rwl.r.unlock();
		}
	}

	protected boolean runningRequest() {
		rwl.w.lock();
		try {
			if (isRunning()) {
				Logging.warn(name + " is already running");
				return false;
			}
			setRunningNow();
			return true;
		} finally {
			rwl.w.unlock();
		}
	}

	public void run(Client client, Variables variables) {
		if (!runningRequest()) {
			Logging.warn("The job " + name + " is already running ("
					+ client.getIndexName() + ")");
			return;
		}
		currentTaskLog = null;
		TaskAbstract currentTask = null;
		try {
			boolean indexHasChanged = false;
			long originalVersion = client.getIndex().getVersion();
			List<TaskItem> taskList = getTaskListCopy();
			for (TaskItem task : taskList) {
				if (isAbort())
					break;
				currentTask = task.getTask();
				setCurrentTaskLog(new TaskLog(task, indexHasChanged, isAbort()));
				addTaskLog(currentTaskLog);
				task.run(client, variables, currentTaskLog);
				currentTaskLog.end();
				if (task.isAbort()) {
					abort();
					Logging.warn("The job " + name + " is aborted");
				}
				if (!indexHasChanged)
					if (client.getIndex().getVersion() != originalVersion)
						indexHasChanged = true;
			}
		} catch (Throwable e) {
			SearchLibException se = e instanceof SearchLibException ? (SearchLibException) e
					: new SearchLibException(e);
			if (currentTaskLog != null)
				currentTaskLog.setError(se);
			setLastError(se);
			Logging.error(se);
			sendErrorEmail(se, currentTask);
		} finally {
			if (currentTaskLog != null)
				currentTaskLog.end();
			runningEnd();
		}
	}

	@Override
	public void abort() {
		super.abort();
		if (currentTaskLog != null)
			currentTaskLog.abortRequested();
	}

	private void sendErrorEmail(Exception error, TaskAbstract currentTask) {
		if (!isEmailNotificationOnFailure())
			return;
		Mailer email = null;
		try {
			email = new Mailer(false, getEmailRecipients(),
					"OpenSearchServer Scheduler Error: " + getName());
			PrintWriter pw = email.getTextPrintWriter();
			pw.println("The scheduler job has failed.");
			pw.print("Name of the job: ");
			pw.println(getName());
			if (currentTask != null) {
				pw.print("Current task: ");
				pw.println(currentTask.getName());
			}
			pw.print("Error message: ");
			pw.println(error.getMessage());
			email.send();
		} catch (EmailException e) {
			Logging.error(e);
		} finally {
			IOUtils.close(email);
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("job", "name", name, "active",
					isActive() ? "yes" : "no", "emailNotificationOnFailure",
					emailNotificationOnFailure ? "yes" : "no",
					"emailRecipients", emailRecipients);
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
		boolean emailNotificationOnFailure = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "emailNotificationOnFailure"));
		String emailRecipients = XPathParser.getAttributeString(node,
				"emailRecipients");
		JobItem jobItem = new JobItem(name);
		jobItem.setEmailNotificationOnFailure(emailNotificationOnFailure);
		jobItem.setEmailRecipients(emailRecipients);
		Node cronNode = xpp.getNode(node, "cron");
		if (cronNode != null)
			jobItem.getCron().fromXml(cronNode);
		jobItem.setActive(active);
		NodeList tasks = xpp.getNodeList(node, "task");
		for (int i = 0; i < tasks.getLength(); i++) {
			TaskItem taskItem = TaskItem.fromXml(config, xpp, tasks.item(i));
			if (taskItem != null)
				jobItem.taskAdd(taskItem);
		}
		return jobItem;
	}

	public void checkTaskExecution(Config config) {
		rwl.r.lock();
		try {
			TaskManager taskManager = TaskManager.getInstance();
			if (isActive())
				taskManager.cronJob(config.getIndexName(), name, cron);
			else
				taskManager.removeJob(config.getIndexName(), name);
		} catch (SearchLibException e) {
			Logging.error(e);
			setLastError(e);
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param lastError
	 *            the lastError to set
	 */
	public void setLastError(SearchLibException lastError) {
		rwl.w.lock();
		try {
			this.lastError = lastError;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the lastError
	 */
	public SearchLibException getLastError() {
		rwl.r.lock();
		try {
			return lastError;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @return the job log
	 */
	public JobLog getJobLog() {
		rwl.r.lock();
		try {
			return jobLog;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addTaskLog(TaskLog taskLog) {
		rwl.w.lock();
		try {
			jobLog.addLog(taskLog);
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		rwl.w.lock();
		try {
			this.name = name;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the emailNotificationOnFailure
	 */
	public boolean isEmailNotificationOnFailure() {
		rwl.r.lock();
		try {
			return emailNotificationOnFailure;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param emailNotificationOnFailure
	 *            the emailNotificationOnFailure to set
	 */
	public void setEmailNotificationOnFailure(boolean emailNotificationOnFailure) {
		rwl.w.lock();
		try {
			this.emailNotificationOnFailure = emailNotificationOnFailure;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the emailRecipients
	 */
	public String getEmailRecipients() {
		rwl.r.lock();
		try {
			return emailRecipients;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param emailRecipients
	 *            the emailRecipients to set
	 */
	public void setEmailRecipients(String emailRecipients) {
		rwl.w.lock();
		try {
			this.emailRecipients = emailRecipients;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the currentTaskLog
	 */
	public TaskLog getCurrentTaskLog() {
		rwl.r.lock();
		try {
			return currentTaskLog;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param currentTaskLog
	 *            the currentTaskLog to set
	 */
	public void setCurrentTaskLog(TaskLog currentTaskLog) {
		rwl.w.lock();
		try {
			this.currentTaskLog = currentTaskLog;
		} finally {
			rwl.w.unlock();
		}
	}
}
