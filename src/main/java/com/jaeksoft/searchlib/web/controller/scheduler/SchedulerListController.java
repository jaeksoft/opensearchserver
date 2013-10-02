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

	public JobItem[] getJobs() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getJobList().getJobs();
	}

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
	public void doExecute(@BindingParam("jobentry") JobItem job)
			throws SearchLibException, NamingException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		if (job.isRunning())
			throw new SearchLibException("The job " + job.getName()
					+ " is already running.");
		TaskManager.getInstance().executeJob(client, job, null);
		reload();
	}

	@Command
	public void doAbort(@BindingParam("jobentry") JobItem job)
			throws SearchLibException, NamingException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		if (!job.isRunning())
			throw new SearchLibException("The job " + job.getName()
					+ " is already stopped.");
		job.abort();
		reload();
	}

	private void doNew(JobItem newJob) {
		setJobItemEdit(newJob);
		setJobItemSelected(null);
	}

	@Command
	@NotifyChange("*")
	public void doClone(@BindingParam("jobentry") JobItem job) {
		JobItem newJob = new JobItem("");
		newJob.copyFrom(job);
		newJob.setName("New job");
		newJob.setActive(false);
		doNew(newJob);
	}

	@Command
	@NotifyChange("*")
	public void onNewJob() throws SearchLibException {
		doNew(new JobItem("New job"));
	}

	@Command
	public void onTimer() throws SearchLibException {
		reload();
	}

}
