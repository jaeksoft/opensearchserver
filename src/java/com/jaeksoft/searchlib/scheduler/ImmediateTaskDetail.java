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

package com.jaeksoft.searchlib.scheduler;

import org.quartz.JobDetail;

public class ImmediateTaskDetail extends JobDetail {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5470927687801196453L;

	private final String jobName;
	private final TaskItem taskItem;

	/**
	 * Immediate job execution
	 * 
	 * @param indexName
	 * @param jobName
	 * @param taskClass
	 */
	public ImmediateTaskDetail(String indexName, String jobName,
			Class<TaskManager> taskClass) {
		super("job|" + jobName + System.currentTimeMillis(), indexName,
				taskClass);
		this.jobName = jobName;
		this.taskItem = null;
	}

	/**
	 * Immediate task execution
	 * 
	 * @param indexName
	 * @param taskItem
	 * @param taskClass
	 */
	public ImmediateTaskDetail(String indexName, TaskItem taskItem,
			Class<TaskManager> taskClass) {
		super("task|" + taskItem.getTask().getClass().getName(), indexName,
				taskClass);
		this.jobName = null;
		this.taskItem = taskItem;
	}

	public String getJobName() {
		return jobName;
	}

	public TaskItem getTaskItem() {
		return taskItem;
	}
}
