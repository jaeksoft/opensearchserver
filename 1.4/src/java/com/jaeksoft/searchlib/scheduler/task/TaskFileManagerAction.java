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
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;

public class TaskFileManagerAction extends TaskAbstract {

	@Override
	public String getName() {
		return "File crawler - Action on selected URLs";
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
		FileManager fileManager = client.getFileManager();
		taskLog.setInfo("File manager Action started");
		if (selectionRequest != null) {
			if (setToUnfetched) {
				taskLog.setInfo("File manager: set selection to unfetched");
				fileManager.updateFetchStatus(selectionRequest,
						FetchStatus.UN_FETCHED, taskLog);
			} else if (deleteSelection) {
				taskLog.setInfo("File manager: delete selection");
				fileManager.delete(selectionRequest, taskLog);
			}
		}
		if (deleteAll) {
			taskLog.setInfo("File manager: Delete All");
			fileManager.deleteAll(taskLog);
		}
		if (optimize) {
			taskLog.setInfo("File manager: optimize");
			fileManager.reload(true, taskLog);
		}
		taskLog.setInfo("File manager Action done");
	}
}
