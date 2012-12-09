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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.process.ThreadAbstract;

public class ImmediateExecution extends ThreadAbstract {

	private Client client;
	private JobItem jobItem;
	private TaskItem taskItem;
	private TaskLog taskLog;

	private ImmediateExecution(Client client) {
		super(client, null);
		this.client = client;
	}

	public ImmediateExecution(Client client, JobItem jobItem) {
		this(client);
		this.jobItem = jobItem;
		this.taskItem = null;
	}

	public ImmediateExecution(Client client, TaskItem taskItem, TaskLog taskLog) {
		this(client);
		this.taskItem = taskItem;
		this.taskLog = taskLog;
		this.jobItem = null;
	}

	@Override
	public void runner() throws Exception {
		if (jobItem != null)
			jobItem.run(client);
		if (taskItem != null)
			taskItem.run(client, taskLog);
	}

	@Override
	public void release() {
	}
}
