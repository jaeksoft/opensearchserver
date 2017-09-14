/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.scheduler;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloadThread;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.util.Variables;

public class ImmediateExecution extends ThreadAbstract<HttpDownloadThread> {

	private final Client client;
	private final JobItem jobItem;
	private final TaskItem taskItem;
	private final Variables variables;
	private final TaskLog taskLog;

	private ImmediateExecution(final Client client, final String taskName, final JobItem jobItem,
			final TaskItem taskItem, final Variables variables, final TaskLog taskLog) {
		super(client, taskName, null, null, taskLog);
		this.client = client;
		this.jobItem = jobItem;
		this.taskItem = taskItem;
		this.variables = variables;
		this.taskLog = taskLog;
	}

	public ImmediateExecution(Client client, JobItem jobItem, Variables variables) {
		this(client, jobItem.getName(), jobItem, null, variables, null);
	}

	public ImmediateExecution(Client client, TaskItem taskItem, TaskLog taskLog) {
		this(client, taskItem.getTask().getName(), null, taskItem, null, taskLog);
	}

	@Override
	public void runner() throws Exception {
		if (jobItem != null)
			jobItem.run(client, variables);
		if (taskItem != null)
			taskItem.run(client, variables, taskLog);
	}

	@Override
	public void release() {
	}
}
