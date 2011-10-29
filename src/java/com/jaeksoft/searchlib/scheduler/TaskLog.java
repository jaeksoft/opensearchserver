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

import java.util.Date;

import com.jaeksoft.searchlib.SearchLibException;

public class TaskLog {

	private Date endDate;

	private Date startDate;

	private long startTime;

	private long endTime;

	private long duration;

	private TaskAbstract taskAbstract;

	private TaskProperty[] taskProperties;

	private SearchLibException error;

	protected TaskLog(TaskItem taskItem) {
		taskAbstract = taskItem.getTask();
		taskProperties = null;
		error = null;
		TaskProperty[] tp = taskItem.getProperties();
		if (tp != null) {
			taskProperties = new TaskProperty[tp.length];
			for (int i = 0; i < taskProperties.length; i++) {
				TaskProperty newTaskProp = new TaskProperty(tp[i]);
				if (newTaskProp.getType() == TaskPropertyType.password)
					newTaskProp.setValue("**hidden**");
				taskProperties[i] = newTaskProp;
			}
		}
		startTime = System.currentTimeMillis();
		endTime = 0;
	}

	protected void end() {
		endTime = System.currentTimeMillis();
		endDate = null;
		duration = endTime - startTime;
	}

	public TaskAbstract getTask() {
		return taskAbstract;
	}

	public TaskProperty[] getProperties() {
		return taskProperties;
	}

	public Date getStartDate() {
		if (startDate == null)
			startDate = new Date(startTime);
		return startDate;
	}

	public Date getEndDate() {
		if (endDate == null && endTime != 0)
			endDate = new Date(endTime);
		return endDate;
	}

	public long getDuration() {
		return duration;
	}

	protected void setError(SearchLibException error) {
		this.error = error;
	}

	public SearchLibException getError() {
		return error;
	}

	@Override
	public String toString() {
		return getStartDate() + getTask().getName();
	}

}
