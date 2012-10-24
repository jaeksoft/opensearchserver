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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class ImmediateTaskDetail extends JobDetail implements JobListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5470927687801196453L;

	private final String jobName;
	private final TaskItem taskItem;
	private boolean executed;

	private final static ReadWriteLock rwl = new ReadWriteLock();

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
		this.executed = false;
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
		this.executed = false;
	}

	public String getJobName() {
		rwl.r.lock();
		try {
			return jobName;
		} finally {
			rwl.r.unlock();
		}
	}

	public TaskItem getTaskItem() {
		rwl.r.lock();
		try {
			return taskItem;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void setExecuted() {
		rwl.w.lock();
		try {
			executed = true;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isExecuted() {
		rwl.r.lock();
		try {
			return executed;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
		setExecuted();
		TaskManager.removeJobListener(this);
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext arg0) {
	}

	@Override
	public void jobWasExecuted(JobExecutionContext arg0,
			JobExecutionException arg1) {
		setExecuted();
		TaskManager.removeJobListener(this);
	}

	public void waitForCompletion(int secTimeOut) throws InterruptedException {
		long finalTime = System.currentTimeMillis() + secTimeOut * 1000;
		while (isExecuted()) {
			if (secTimeOut != 0)
				if (System.currentTimeMillis() > finalTime)
					return;
			Thread.sleep(100);
		}
	}

}
