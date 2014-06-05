/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.replication.ReplicationList;
import com.jaeksoft.searchlib.replication.ReplicationMaster;
import com.jaeksoft.searchlib.replication.ReplicationType;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

@AfterCompose(superclass = true)
public class ReplicationController extends CommonController {

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

	@NotifyChange("*")
	public void setSelectedItem(ReplicationItem item) throws SearchLibException {
		selectedItem = item;
		currentItem = new ReplicationItem(selectedItem);
	}

	public ReplicationItem getItem() {
		return currentItem;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedItem == null ? "Create a new target"
				: "Edit the selected target";
	}

	public boolean isSelected() {
		return selectedItem != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws InterruptedException, SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		client.getReplicationList().save(selectedItem, currentItem);
		client.saveReplicationList();
		onCancel();
	}

	public boolean isRefresh() throws SearchLibException {
		ReplicationMaster replicationMaser = getReplicationMaster();
		if (replicationMaser == null)
			return false;
		return replicationMaser.getThreadsCount() > 0;
	}

	@Command
	@NotifyChange("replicationList")
	public void onTimer() throws SearchLibException {
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		currentItem = new ReplicationItem(getReplicationMaster());
		selectedItem = null;
	}

	@Command
	@NotifyChange("*")
	public void onDelete() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		if (client == null)
			return;
		client.getReplicationList().save(selectedItem, null);
		client.saveReplicationList();
		onCancel();
	}

	public ReplicationList getReplicationList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getReplicationList();
	}

	private ReplicationMaster getReplicationMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getReplicationMaster();
	}

	public List<String> getSearchRequests() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> nameList = new ArrayList<String>();
		client.getRequestMap().getNameList(nameList,
				RequestTypeEnum.SearchFieldRequest,
				RequestTypeEnum.SearchRequest);
		return nameList;
	}

	@Command
	@NotifyChange("*")
	public void execute(@BindingParam("item") ReplicationItem item)
			throws InterruptedException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		getReplicationMaster().execute(client, item, false, null, null);
	}

	public ReplicationType[] getTypeValues() {
		return ReplicationType.values();
	}

}
