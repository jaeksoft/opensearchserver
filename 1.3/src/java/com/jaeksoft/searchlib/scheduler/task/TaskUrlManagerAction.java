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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;

public class TaskUrlManagerAction extends TaskAbstract {

	@Override
	public String getName() {
		return "Web crawler - Action on selected URLs";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return null;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	private boolean deleteAll = false;

	public void setDeleteAll() {
		deleteAll = true;
	}

	private boolean optimize = false;

	public void setOptimize() {
		optimize = true;
	}

	private SearchRequest selectionRequest = null;

	private boolean deleteSelection;

	private boolean setToUnfetched;

	public void setSelection(SearchRequest selectionRequest,
			boolean deleteSelection, boolean setToUnfetched) {
		this.selectionRequest = selectionRequest;
		this.setToUnfetched = setToUnfetched;
		this.deleteSelection = deleteSelection;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			TaskLog taskLog) throws SearchLibException, IOException {
		UrlManager urlManager = client.getUrlManager();
		taskLog.setInfo("URL manager Action started");
		if (selectionRequest != null) {
			if (setToUnfetched) {
				taskLog.setInfo("URL manager: set selection to unfetched");
				urlManager.updateFetchStatus(selectionRequest,
						FetchStatus.UN_FETCHED, taskLog);
			} else if (deleteSelection) {
				taskLog.setInfo("URL manager: delete selection");
				urlManager.deleteUrls(selectionRequest, taskLog);
			}
		}
		if (deleteAll) {
			taskLog.setInfo("URL manager: Delete All");
			urlManager.deleteAll(taskLog);
		}
		if (optimize) {
			taskLog.setInfo("URL manager: optimize");
			urlManager.reload(true, taskLog);
		}
		taskLog.setInfo("URL manager Action done");
	}
}
