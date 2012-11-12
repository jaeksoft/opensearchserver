/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public abstract class AbstractManager {

	protected Client targetClient;

	private Set<TaskAbstract> taskToCheck;
	private TaskLog currentTaskLog;

	protected AbstractManager() {
		currentTaskLog = null;
		targetClient = null;
		taskToCheck = new HashSet<TaskAbstract>(0);
	}

	protected void init(Client client) {
		targetClient = client;
	}

	public void setCurrentTaskLog(TaskLog taskLog) throws SearchLibException {
		synchronized (this) {
			if (currentTaskLog != null)
				throw new SearchLibException("A task is already running: "
						+ currentTaskLog.getTask().getName());
			if (taskLog != null)
				synchronized (taskToCheck) {
					taskToCheck.add(taskLog.getTask());
				}
			currentTaskLog = taskLog;
		}
	}

	public void resetCurrentTaskLog() {
		synchronized (this) {
			currentTaskLog = null;
		}
	}

	public TaskLog getCurrentTaskLog() {
		synchronized (this) {
			return currentTaskLog;
		}
	}

	public boolean isCurrentTaskLog() {
		return getCurrentTaskLog() != null;
	}

	public boolean waitForTask(TaskAbstract taskAbstract, int secTimeOut)
			throws InterruptedException {
		long timeOut = System.currentTimeMillis() + 1000 * secTimeOut;
		while (timeOut > System.currentTimeMillis()) {
			synchronized (taskToCheck) {
				if (taskToCheck.remove(taskAbstract))
					return true;
			}
			Thread.sleep(1000);
		}
		Logging.warn("Wait for task " + taskAbstract.getName());
		return false;
	}

	public static Date getPastDate(long fetchInterval, String intervalUnit) {
		long l;
		if ("hours".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 3600;
		else if ("minutes".equalsIgnoreCase(intervalUnit))
			l = fetchInterval * 1000 * 60;
		else
			// Default is days
			l = fetchInterval * 1000 * 86400;
		return new Date(System.currentTimeMillis() - l);
	}

}
