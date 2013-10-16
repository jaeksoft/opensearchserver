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

import com.jaeksoft.searchlib.scheduler.task.TaskBuildAutocompletion;
import com.jaeksoft.searchlib.scheduler.task.TaskDatabaseCrawlerRun;
import com.jaeksoft.searchlib.scheduler.task.TaskDatabaseScript;
import com.jaeksoft.searchlib.scheduler.task.TaskDeleteAll;
import com.jaeksoft.searchlib.scheduler.task.TaskDeleteQuery;
import com.jaeksoft.searchlib.scheduler.task.TaskFileCrawlerStart;
import com.jaeksoft.searchlib.scheduler.task.TaskFileCrawlerStop;
import com.jaeksoft.searchlib.scheduler.task.TaskFlushCrawlCache;
import com.jaeksoft.searchlib.scheduler.task.TaskFtpXmlFeed;
import com.jaeksoft.searchlib.scheduler.task.TaskMergeDataIndex;
import com.jaeksoft.searchlib.scheduler.task.TaskOptimizeIndex;
import com.jaeksoft.searchlib.scheduler.task.TaskOtherScheduler;
import com.jaeksoft.searchlib.scheduler.task.TaskPullFields;
import com.jaeksoft.searchlib.scheduler.task.TaskPullTerms;
import com.jaeksoft.searchlib.scheduler.task.TaskQueryCheck;
import com.jaeksoft.searchlib.scheduler.task.TaskReplicationRun;
import com.jaeksoft.searchlib.scheduler.task.TaskReportLoadLogFile;
import com.jaeksoft.searchlib.scheduler.task.TaskRestCrawlerRun;
import com.jaeksoft.searchlib.scheduler.task.TaskSleep;
import com.jaeksoft.searchlib.scheduler.task.TaskUploadMonitor;
import com.jaeksoft.searchlib.scheduler.task.TaskUrlManagerAction;
import com.jaeksoft.searchlib.scheduler.task.TaskWebCrawlerStart;
import com.jaeksoft.searchlib.scheduler.task.TaskWebCrawlerStop;
import com.jaeksoft.searchlib.scheduler.task.TaskXmlLoad;
import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class TaskEnum extends ExtensibleEnum<TaskEnumItem> {

	public TaskEnum() {
		new TaskEnumItem(this, TaskBuildAutocompletion.class);

		new TaskEnumItem(this, TaskDatabaseCrawlerRun.class);

		new TaskEnumItem(this, TaskDatabaseScript.class);

		new TaskEnumItem(this, TaskDeleteQuery.class);

		new TaskEnumItem(this, TaskDeleteAll.class);

		new TaskEnumItem(this, TaskFileCrawlerStart.class);

		new TaskEnumItem(this, TaskFileCrawlerStop.class);

		new TaskEnumItem(this, TaskMergeDataIndex.class);

		new TaskEnumItem(this, TaskOptimizeIndex.class);

		new TaskEnumItem(this, TaskOtherScheduler.class);

		new TaskEnumItem(this, TaskReplicationRun.class);

		new TaskEnumItem(this, TaskRestCrawlerRun.class);

		new TaskEnumItem(this, TaskSleep.class);

		new TaskEnumItem(this, TaskUploadMonitor.class);

		new TaskEnumItem(this, TaskUrlManagerAction.class);

		new TaskEnumItem(this, TaskWebCrawlerStart.class);

		new TaskEnumItem(this, TaskWebCrawlerStop.class);

		new TaskEnumItem(this, TaskXmlLoad.class);

		new TaskEnumItem(this, TaskFtpXmlFeed.class);

		new TaskEnumItem(this, TaskPullTerms.class);

		new TaskEnumItem(this, TaskPullFields.class);

		new TaskEnumItem(this, TaskFlushCrawlCache.class);

		new TaskEnumItem(this, TaskReportLoadLogFile.class);

		new TaskEnumItem(this, TaskQueryCheck.class);

	}

	/**
	 * Find the TaskAbstract using the simple name of the class
	 * 
	 * @param taskClass
	 * @return
	 */
	public TaskAbstract findClass(String taskClass) {
		for (TaskEnumItem item : getList())
			if (taskClass.equals(item.getTask().getClass().getSimpleName()))
				return item.getTask();
		return null;
	}
}
