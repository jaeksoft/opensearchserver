/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexStatistics;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskDeleteAll;
import com.jaeksoft.searchlib.scheduler.task.TaskMergeDataIndex;
import com.jaeksoft.searchlib.scheduler.task.TaskOptimizeIndex;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.webservice.command.CommandImpl;

@AfterCompose(superclass = true)
public class CommandsController extends CommonController {

	private TaskItem taskOptimize = null;
	private TaskItem taskTruncate = null;
	private TaskItem taskMerge = null;

	private String mergeIndex = null;

	private class DeleteAlert extends AlertController {

		protected DeleteAlert() throws InterruptedException {
			super(
					"Please, confirm that you want to delete all the documents in the main index",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
		}

		@Override
		protected void onYes() throws SearchLibException, InterruptedException {
			Client client = getClient();
			if (client == null)
				return;
			if (isRunningTruncate())
				throw new SearchLibException("Truncation is already running.");
			taskTruncate = new TaskItem(client, new TaskDeleteAll());
			TaskManager.executeTask(client, taskTruncate, null);
			reload();
		}
	}

	public boolean isRunningOptimize() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return false;
		if (taskOptimize != null) {
			if (taskOptimize.isRunning())
				return true;
			if (taskOptimize.getLastExecution() == null)
				return true;
			taskOptimize = null;
		}
		return client.isOptimizing();
	}

	public boolean isRunningTruncate() {
		if (taskTruncate == null)
			return false;
		if (taskTruncate.isRunning())
			return true;
		if (taskTruncate.getLastExecution() == null)
			return true;
		taskTruncate = null;
		return false;
	}

	public boolean isRunningMerge() {
		if (taskMerge == null)
			return false;
		if (taskMerge.isRunning())
			return true;
		if (taskMerge.getLastExecution() == null)
			return true;
		taskMerge = null;
		return false;
	}

	public boolean isTaskRunning() throws SearchLibException {
		return isRunningOptimize() || isRunningTruncate() || isRunningMerge();
	}

	public CommandsController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() throws SearchLibException {
		isTaskRunning();
	}

	@Command
	@NotifyChange("*")
	public void onReloadClient() throws SearchLibException, IOException {
		synchronized (this) {
			getClient().reload();
		}
	}

	@Command
	@NotifyChange("*")
	public void onTimer() throws SearchLibException {
	}

	@Command
	@NotifyChange("*")
	public void onOptimize() throws SearchLibException, IOException,
			URISyntaxException, InterruptedException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			if (isRunningOptimize())
				throw new SearchLibException(
						"The optimization is already running");
			taskOptimize = new TaskItem(client, new TaskOptimizeIndex());
			TaskManager.executeTask(client, taskOptimize, null);
		}
	}

	public String getOptimizeStatus() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (client.isOptimizing())
				return "Running";
			IndexStatistics stats = client.getStatistics();
			if (stats == null)
				return null;
			return stats.isOptimized() ? "Optimized" : "Not optimized";
		}
	}

	public String getDocumentNumber() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			IndexStatistics stats = client.getStatistics();
			if (stats == null)
				return null;
			return stats.getNumDocs() + " document(s).";
		}
	}

	@Command
	public void onDeleteAll() throws InterruptedException {
		synchronized (this) {
			new DeleteAlert();
		}
	}

	public List<String> getIndexList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<String> list = new ArrayList<String>(0);
			String currentName = client.getIndexName();
			for (ClientCatalogItem item : ClientCatalog
					.getClientCatalog(getLoggedUser())) {
				String v = item.getIndexName();
				if (!v.equals(currentName))
					list.add(v);
			}
			return list;
		}
	}

	@Command
	@NotifyChange("*")
	public void onMergeData() throws SearchLibException, IOException,
			URISyntaxException, InterruptedException {
		synchronized (this) {
			User user = getLoggedUser();
			Client client = getClient();
			if (client == null)
				return;
			if (isRunningMerge())
				throw new SearchLibException("A merge is already running");
			TaskMergeDataIndex taskMergeDataIndex = new TaskMergeDataIndex();
			taskMerge = new TaskItem(client, taskMergeDataIndex);

			taskMergeDataIndex.setValues(taskMerge.getProperties(), mergeIndex,
					user != null ? user.getName() : null,
					user != null ? user.getApiKey() : null);
			TaskManager.executeTask(client, taskMerge, null);
		}
	}

	public String getMergeStatus() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getMergeStatus();
		}
	}

	@NotifyChange("*")
	public void setMergeIndex(String index) {
		synchronized (this) {
			this.mergeIndex = index;
		}
	}

	public String getMergeIndex() {
		synchronized (this) {
			return this.mergeIndex;
		}
	}

	public String getReloadXmlApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getReloadXML(getLoggedUser(), getClient());
	}

	public String getReloadJsonApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getReloadJSON(getLoggedUser(), getClient());
	}

	public String getOptimizeXmlApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getOptimizeXML(getLoggedUser(), getClient());
	}

	public String getOptimizeJsonApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getOptimizeJSON(getLoggedUser(), getClient());
	}

	public String getTruncateXmlApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getTruncateXML(getLoggedUser(), getClient());
	}

	public String getTruncateJsonApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getTruncateJSON(getLoggedUser(), getClient());
	}

	public String getMergeXmlApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getMergeXML(getLoggedUser(), getClient(),
				getMergeIndex());
	}

	public String getMergeJsonApi() throws UnsupportedEncodingException,
			SearchLibException {
		return CommandImpl.getMergeJSON(getLoggedUser(), getClient(),
				getMergeIndex());
	}
}
