/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import java.net.URISyntaxException;

import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexMode;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskDeleteAll;
import com.jaeksoft.searchlib.scheduler.task.TaskOptimizeIndex;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CommandsController extends CommonController {

	private class DeleteAlert extends AlertController {

		protected DeleteAlert() throws InterruptedException {
			super(
					"Please, confirm that you want to delete all the documents in the main index",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			if (client == null)
				return;
			TaskItem taskItem = new TaskItem(client, new TaskDeleteAll());
			TaskManager.executeTask(client, taskItem);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7911006190658783502L;

	public CommandsController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
	}

	@Override
	public void onReload() throws SearchLibException {
		synchronized (this) {
			getClient().reload();
			reloadPage();
		}
	}

	public void onReadOnly() throws SearchLibException {
		synchronized (this) {
			getClient().setReadWriteMode(IndexMode.READ_ONLY);
			reloadPage();
		}
	}

	public void onReadWrite() throws SearchLibException {
		synchronized (this) {
			getClient().setReadWriteMode(IndexMode.READ_WRITE);
			reloadPage();
		}
	}

	public void onOnline() throws SearchLibException {
		synchronized (this) {
			getClient().setOnline(true);
			reloadPage();
		}
	}

	public void onOffline() throws SearchLibException {
		synchronized (this) {
			getClient().setOnline(false);
			reloadPage();
		}
	}

	public void onOptimize() throws SearchLibException, IOException,
			URISyntaxException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			if (client.isOptimizing())
				throw new SearchLibException(
						"The optimization is already running");
			TaskItem taskItem = new TaskItem(client, new TaskOptimizeIndex());
			TaskManager.executeTask(client, taskItem);
			reloadPage();
		}
	}

	public String getReadOnlyStatus() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (!client.isOnline())
				return "Unknown";
			return client.getReadWriteMode().getLabel();
		}
	}

	public String getOnlineStatus() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.isOnline() ? "Online" : "Offline";
		}
	}

	public String getOptimizeStatus() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (!client.isOnline())
				return "Unknown";
			if (client.isOptimizing())
				return "Running";
			return client.getStatistics().isOptimized() ? "Optimized"
					: "Not optimized";
		}
	}

	public boolean isOnline() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return false;
			return client.isOnline();
		}
	}

	public boolean isOffline() throws SearchLibException {
		return !isOnline();
	}

	public boolean isReadOnly() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return false;
			return client.getReadWriteMode() == IndexMode.READ_ONLY;
		}
	}

	public boolean isReadWrite() throws SearchLibException {
		return !isReadOnly();
	}

	public String getDocumentNumber() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (!client.isOnline())
				return "Unknown";
			return client.getStatistics().getNumDocs() + " document(s).";
		}
	}

	public void onDeleteAll() throws InterruptedException {
		synchronized (this) {
			new DeleteAlert();
		}
	}

}
