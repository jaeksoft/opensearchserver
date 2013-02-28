/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.scheduler;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class SchedulerImpl extends CommonServices implements SoapScheduler,
		RestScheduler {

	private JobItem getJobItem(Client client, String name)
			throws SearchLibException {
		JobItem jobItem = client.getJobList().get(name);
		if (jobItem == null)
			throw new WebServiceException("Scheduler not found (" + name + ")");
		return jobItem;
	}

	@Override
	public CommonResult status(String use, String login, String key, String name) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_SCHEDULER);
			ClientFactory.INSTANCE.properties.checkApi();
			return new SchedulerResult(getJobItem(client, name));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}

	}

	@Override
	public CommonResult statusXML(String use, String login, String key,
			String name) {
		return status(use, login, key, name);
	}

	@Override
	public CommonResult statusJSON(String use, String login, String key,
			String name) {
		return status(use, login, key, name);
	}

	@Override
	public CommonResult run(String use, String login, String key, String name) {
		try {
			Client client = getLoggedClient(use, login, key, Role.SCHEDULER_RUN);
			ClientFactory.INSTANCE.properties.checkApi();
			JobItem jobItem = getJobItem(client, name);
			TaskManager.getInstance().executeJob(client, jobItem);
			return new SchedulerResult(jobItem);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}

	}

	@Override
	public CommonResult runXML(String use, String login, String key, String name) {
		return status(use, login, key, name);
	}

	@Override
	public CommonResult runJSON(String use, String login, String key,
			String name) {
		return status(use, login, key, name);
	}
}
