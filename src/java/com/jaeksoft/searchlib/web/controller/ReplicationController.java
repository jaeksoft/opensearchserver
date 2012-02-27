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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Component;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.replication.ReplicationMaster;

public class ReplicationController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1517834105476217906L;

	private transient ReplicationItem selectedItem;

	private transient ReplicationItem currentItem;

	public ReplicationController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedItem = null;
		currentItem = null;
		ReplicationMaster rm = getReplicationMaster();
		if (rm != null)
			currentItem = new ReplicationItem(getReplicationMaster());
	}

	public ReplicationItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(ReplicationItem item) {
		selectedItem = item;
		currentItem = new ReplicationItem(selectedItem);
		reloadPage();
	}

	public ReplicationItem getItem() {
		return currentItem;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedItem == null ? "Create a new target"
				: "Edit the selected target";
	}

	public boolean selected() {
		return selectedItem != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	public void onSave() throws InterruptedException, SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		if (selectedItem != null)
			selectedItem.copy(currentItem);
		else
			client.getReplicationList().put(currentItem);
		client.saveReplicationList();
		onCancel();
	}

	public boolean isRefresh() throws SearchLibException {
		ReplicationMaster replicationMaser = getReplicationMaster();
		if (replicationMaser == null)
			return false;
		return replicationMaser.getThreadsCount() > 0;
	}

	public void onTimer() {
		super.reloadPage();
	}

	public void onCancel() throws SearchLibException {
		currentItem = new ReplicationItem(getReplicationMaster());
		selectedItem = null;
		reloadPage();
	}

	public void onDelete() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		if (client == null)
			return;
		client.getReplicationList().remove(selectedItem);
		client.saveReplicationList();
		onCancel();
	}

	public Set<ReplicationItem> getReplicationSet() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getReplicationList().getSet();
	}

	private ReplicationItem getReplicationItem(Component comp) {
		if (comp == null)
			return null;
		return (ReplicationItem) comp.getAttribute("replicationitem");
	}

	private ReplicationMaster getReplicationMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getReplicationMaster();
	}

	public void execute(Component comp) throws InterruptedException,
			SearchLibException {
		ReplicationItem item = getReplicationItem(comp);
		if (item == null)
			return;
		Client client = getClient();
		if (client == null)
			return;
		getReplicationMaster().execute(client, item, false, null);
		reloadPage();
	}

}
