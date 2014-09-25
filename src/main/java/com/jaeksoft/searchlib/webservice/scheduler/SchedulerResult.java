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
package com.jaeksoft.searchlib.webservice.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.JobLog;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class SchedulerResult extends CommonResult {

	public final boolean isRunning;

	public final boolean isActive;

	public final String lastError;

	@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
	public final Date lastExecutionDate;

	public final List<TaskInfo> taskInfos;

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@JsonInclude(Include.NON_EMPTY)
	public static class TaskInfo {

		public final String name;

		@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
		public final Date startDate;

		@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
		public final Date endDate;

		public final int duration;

		public final String infos;

		public TaskInfo() {
			name = null;
			startDate = null;
			endDate = null;
			duration = 0;
			infos = null;
		}

		public TaskInfo(TaskLog taskLog) {
			name = taskLog.getTask().getName();
			startDate = taskLog.getStartDate();
			endDate = taskLog.getEndDate();
			duration = (int) (taskLog.getDuration() / 1000);
			infos = taskLog.getInfo();
		}
	}

	public SchedulerResult() {
		isRunning = false;
		isActive = false;
		lastError = null;
		lastExecutionDate = null;
		taskInfos = null;
	}

	public SchedulerResult(JobItem jobItem) {
		super(true, null);
		isRunning = jobItem.isRunning();
		isActive = jobItem.isActive();
		SearchLibException e = jobItem.getLastError();
		lastError = e != null ? e.getMessage() : null;
		lastExecutionDate = jobItem.getLastExecution();
		taskInfos = new ArrayList<TaskInfo>();
		JobLog jobLog = jobItem.getJobLog();
		if (jobLog == null)
			return;
		TaskLog[] taskLogs = jobLog.getLogs();
		if (taskLogs == null)
			return;
		for (TaskLog taskLog : taskLogs)
			taskInfos.add(new TaskInfo(taskLog));
	}
}
