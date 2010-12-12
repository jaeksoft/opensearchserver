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

import com.jaeksoft.searchlib.scheduler.task.TaskDatabaseCrawlerRun;
import com.jaeksoft.searchlib.scheduler.task.TaskFileCrawlerStart;
import com.jaeksoft.searchlib.scheduler.task.TaskFileCrawlerStop;
import com.jaeksoft.searchlib.scheduler.task.TaskOptimizeIndex;
import com.jaeksoft.searchlib.scheduler.task.TaskWebCrawlerStart;
import com.jaeksoft.searchlib.scheduler.task.TaskWebCrawlerStop;

public enum TaskEnum {

	DatabaseCrawlerRun(TaskDatabaseCrawlerRun.class),

	FileCrawlerStart(TaskFileCrawlerStart.class),

	FileCrawlerStop(TaskFileCrawlerStop.class),

	OptimizeIndex(TaskOptimizeIndex.class),

	WebCrawlerStart(TaskWebCrawlerStart.class),

	WebCrawlerStop(TaskWebCrawlerStop.class);

	private TaskAbstract task;

	private TaskEnum(Class<? extends TaskAbstract> taskClass) {
		try {
			this.task = taskClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @return the task abstract
	 */
	public TaskAbstract getTask() {
		return task;
	}

	@Override
	public String toString() {
		return task.getName();
	}

	/**
	 * Find the TaskAbstract using the simple name of the class
	 * 
	 * @param taskClass
	 * @return
	 */
	public static TaskAbstract findClass(String taskClass) {
		for (TaskEnum te : values())
			if (taskClass.equals(te.task.getClass().getSimpleName()))
				return te.getTask();
		return null;
	}
}
