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

import java.util.Date;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class TaskLog implements InfoCallback {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Date endDate;

	private Date startDate;

	private long startTime;

	private long endTime;

	private long duration;

	final private boolean indexHasChanged;

	private TaskAbstract taskAbstract;

	private TaskProperty[] taskProperties;

	private SearchLibException error;

	private String info;

	protected TaskLog(TaskItem taskItem, boolean indexHasChanged) {
		taskAbstract = taskItem.getTask();
		taskProperties = null;
		this.indexHasChanged = indexHasChanged;
		error = null;
		info = null;
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
		rwl.w.lock();
		try {
			endTime = System.currentTimeMillis();
			endDate = null;
			duration = endTime - startTime;
		} finally {
			rwl.w.unlock();
		}
	}

	public TaskAbstract getTask() {
		rwl.r.lock();
		try {
			return taskAbstract;
		} finally {
			rwl.r.unlock();
		}
	}

	public TaskProperty[] getProperties() {
		rwl.r.lock();
		try {
			return taskProperties;
		} finally {
			rwl.r.unlock();
		}
	}

	public Date getStartDate() {
		rwl.r.lock();
		try {
			if (startDate == null)
				startDate = new Date(startTime);
			return startDate;
		} finally {
			rwl.r.unlock();
		}
	}

	public Date getEndDate() {
		rwl.r.lock();
		try {
			if (endDate != null)
				return endDate;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (endDate == null && endTime != 0)
				endDate = new Date(endTime);
			return endDate;
		} finally {
			rwl.w.unlock();
		}
	}

	public long getDuration() {
		rwl.r.lock();
		try {
			return duration;
		} finally {
			rwl.r.unlock();
		}
	}

	protected void setError(SearchLibException error) {
		rwl.w.lock();
		try {
			this.error = error;
		} finally {
			rwl.w.unlock();
		}
	}

	public SearchLibException getError() {
		rwl.r.lock();
		try {
			return error;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setInfo(String info) {
		rwl.w.lock();
		try {
			this.info = info;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getInfo() {
		rwl.r.lock();
		try {
			return info;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		return getStartDate() + getTask().getName();
	}

	public boolean isIndexHasChanged() {
		rwl.r.lock();
		try {
			return indexHasChanged;
		} finally {
			rwl.r.unlock();
		}
	}

}
