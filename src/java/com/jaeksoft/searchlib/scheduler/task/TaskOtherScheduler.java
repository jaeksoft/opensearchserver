/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler.task;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.SearchLibException.AbortException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskOtherScheduler extends TaskAbstract {

	final private TaskPropertyDef propSchedulerName = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Scheduler name", 50);

	final private TaskPropertyDef propAction = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Which action", 50);

	final private TaskPropertyDef propTimeOut = new TaskPropertyDef(
			TaskPropertyType.textBox, "Time out", 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSchedulerName,
			propAction, propTimeOut };

	@Override
	public String getName() {
		return "Other Scheduler";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		if (propertyDef == propSchedulerName) {
			JobList jobList = config.getJobList();
			List<String> nameList = new ArrayList<String>(jobList.getCount());
			jobList.populateNameList(nameList);
			if (nameList.size() == 0)
				return null;
			String[] values = new String[nameList.size()];
			nameList.toArray(values);
			return values;
		} else if (propertyDef == propAction) {
			return ClassPropertyEnum.SCHEDULER_ACTION_LIST;
		}
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propAction)
			return ClassPropertyEnum.SCHEDULER_ACTION_LIST[0];
		if (propertyDef == propTimeOut)
			return "60";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException, InterruptedException {
		String jobName = properties.getValue(propSchedulerName);
		JobItem job = client.getJobList().get(jobName);
		if (job == null)
			throw new SearchLibException("Job " + jobName + " not found");
		String action = properties.getValue(propAction);
		String p = properties.getValue(propTimeOut);
		int timeOut = p == null ? 60 : Integer.parseInt(p);

		if (ClassPropertyEnum.SCHEDULER_ACTION_LIST[0].equals(action))
			waitForCompletion(client, job, timeOut, taskLog);
		else if (ClassPropertyEnum.SCHEDULER_ACTION_LIST[1].equals(action))
			exitIfRunning(client, job, taskLog);
	}

	private void exitIfRunning(Client client, JobItem job, TaskLog taskLog)
			throws AbortException {
		if (!job.isRunning())
			return;
		taskLog.setInfo("Aborting");
		throw new SearchLibException.AbortException();
	}

	private void waitForCompletion(Client client, JobItem job, int secTimeOut,
			TaskLog taskLog) throws AbortException, InterruptedException {
		long t = System.currentTimeMillis();
		if (job.waitForEnd(secTimeOut)) {
			t = (System.currentTimeMillis() - t) / 1000;
			taskLog.setInfo("Sleeping time: " + t);
			if (taskLog.isAbortRequested())
				throw new AbortException();
			return;
		}
		taskLog.setInfo("Time out reached - Aborting");
		throw new SearchLibException.AbortException();
	}
}
