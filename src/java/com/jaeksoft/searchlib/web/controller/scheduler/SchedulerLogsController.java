/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class SchedulerLogsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7922439239702754021L;

	private transient JobItem selectedJob;

	public SchedulerLogsController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedJob = null;
	}

	@Override
	public void eventJobEdit(JobItem jobItem) throws SearchLibException {
		reloadPage();
	}

	public JobItem[] getJobs() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		JobItem[] jobList = client.getJobList().getJobs();
		if (jobList != null && selectedJob == null)
			if (jobList.length > 0)
				selectedJob = jobList[0];
		return jobList;
	}

	public JobItem getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(JobItem job) {
		selectedJob = job;
		reloadPage();
	}

	public boolean isJobSelected() {
		return selectedJob != null;
	}

}
