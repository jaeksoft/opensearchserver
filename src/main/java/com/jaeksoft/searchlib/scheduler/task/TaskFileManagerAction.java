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
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.utils.Variables;

public class TaskFileManagerAction extends TaskAbstract {

	final private TaskPropertyDef propCommand = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Command", "Command",
			"Select the command to execute", 30);

	final private TaskPropertyDef propBufferSize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Buffer size", "Buffer size",
			"Buffer size", 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propCommand,
			propBufferSize };

	final public static String CommandDoNothing = "Do nothing";
	final public static String CommandDeleteAll = "Delete all";
	final public static String CommandOptimize = "Optimize";
	final public static String CommandSynchronize = "Synchronize";

	final private static String[] CommandList = { CommandDoNothing,
			CommandDeleteAll, CommandSynchronize, CommandOptimize };

	@Override
	public String getName() {
		return "File crawler - URI Database";
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
		else if (propertyDef == propBufferSize)
			return "10000";
		return null;
	}

	private AbstractSearchRequest selectionRequest = null;

	private FetchStatus setToFetchStatus = null;

	private String manualCommand = null;

	private Integer manualBufferSize = null;

	public void setManual(AbstractSearchRequest selectionRequest,
			FetchStatus setToFetchStatus, String manualCommand, int bufferSize) {
		this.selectionRequest = selectionRequest;
		this.setToFetchStatus = setToFetchStatus;
		this.manualCommand = manualCommand;
		this.manualBufferSize = bufferSize;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException,
			IOException {
		FileManager fileManager = client.getFileManager();
		taskLog.setInfo("File manager Action started");

		String command = manualCommand != null ? manualCommand : properties
				.getValue(propCommand);

		int bufferSize = manualBufferSize != null ? manualBufferSize : Integer
				.parseInt(properties.getValue(propBufferSize));

		if (selectionRequest != null) {
			if (setToFetchStatus != null) {
				taskLog.setInfo("File manager: set selection to unfetched");
				fileManager.updateFetchStatus(selectionRequest,
						setToFetchStatus, bufferSize, taskLog);
			}
			if (CommandDeleteAll.equals(command))
				taskLog.setInfo("File manager: delete selection");
			fileManager.delete(selectionRequest, taskLog);
			return;
		}
		if (CommandDeleteAll.equals(command)) {
			taskLog.setInfo("File manager: Delete All");
			fileManager.deleteAll(taskLog);
		} else if (CommandOptimize.equals(command)) {
			taskLog.setInfo("File manager: optimize");
			fileManager.reload(true, taskLog);
		}
		taskLog.setInfo("File manager Action done");
	}
}
