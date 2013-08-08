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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;
import java.util.Map;

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
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskUrlManagerAction extends TaskAbstract {

	final private TaskPropertyDef propCommand = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Command", "Command",
			"Select the command to execute", 30);

	final private TaskPropertyDef[] taskPropertyDefs = { propCommand };

	final private static String CommandDoNothing = "Do nothing";
	final private static String CommandDeleteAll = "Delete all";
	final private static String CommandLoadSitemap = "Load Sitemap(s)";
	final private static String CommandOptimize = "Optimize";

	final private static String[] CommandList = { CommandDoNothing,
			CommandDeleteAll, CommandLoadSitemap, CommandOptimize };

	@Override
	public String getName() {
		return "Web crawler - URL database";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties) {
		if (propertyDef == propCommand)
			return CommandList;
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propCommand)
			return CommandList[0];
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

	private boolean doSiteMaps;

	private boolean deleteSelection;

	private FetchStatus setToFetchStatus;

	private int bufferSize = 10000;

	public void setSelection(boolean doSiteMaps,
			SearchRequest selectionRequest, boolean deleteSelection,
			FetchStatus setToFetchStatus, int bufferSize) {
		this.doSiteMaps = doSiteMaps;
		this.selectionRequest = selectionRequest;
		this.setToFetchStatus = setToFetchStatus;
		this.deleteSelection = deleteSelection;
		this.bufferSize = bufferSize;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Map<String, String> variables, TaskLog taskLog)
			throws SearchLibException, IOException {
		UrlManager urlManager = client.getUrlManager();
		taskLog.setInfo("URL manager Action started");

		String command = properties.getValue(propCommand);

		if (selectionRequest != null) {
			if (setToFetchStatus != null) {
				taskLog.setInfo("URL manager: set selection to: "
						+ setToFetchStatus.getName());
				urlManager.updateFetchStatus(selectionRequest,
						setToFetchStatus, bufferSize, taskLog);
			} else if (deleteSelection) {
				taskLog.setInfo("URL manager: delete selection");
				urlManager.deleteUrls(selectionRequest, bufferSize, taskLog);
			}
		}
		if (doSiteMaps || CommandLoadSitemap.equals(command)) {
			taskLog.setInfo("URL manager: Handle SiteMaps");
			urlManager.updateSiteMap(taskLog);
		}
		if (deleteAll || CommandDeleteAll.equals(command)) {
			taskLog.setInfo("URL manager: Delete All");
			urlManager.deleteAll(taskLog);
		}
		if (optimize || CommandOptimize.equals(command)) {
			taskLog.setInfo("URL manager: optimize");
			urlManager.reload(true, taskLog);
		}
	}
}
