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

package com.jaeksoft.searchlib.web.controller.scheduler;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;

public class SchedulerListController extends SchedulerController {

	public SchedulerListController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public JobItem[] getJobs() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getJobList().getJobs();
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return false;
		return client.getJobList().getActiveCount() > 0;
	}

	@Command
	@NotifyChange("*")
	public void doEdit(@BindingParam("jobentry") JobItem selectedJob)
			throws SearchLibException {
		setJobItemSelected(selectedJob);
		JobItem currentJob = new JobItem(null);
		currentJob.copyFrom(selectedJob);
		setJobItemEdit(currentJob);
	}

	@Command
	@NotifyChange("*")
	public void doExecute(@BindingParam("jobentry") JobItem job)
			throws SearchLibException, NamingException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		if (job.isRunning())
			throw new SearchLibException("The job " + job.getName()
					+ " is already running.");
		TaskManager.executeJob(client, job);
		reload();
	}

	@Command
	@NotifyChange("*")
	public void onNewJob() throws SearchLibException {
		setJobItemEdit(new JobItem("New job"));
		setJobItemSelected(null);
	}

	@Command
	@Override
	public void onTimer() throws SearchLibException {
		reload();
	}

}
