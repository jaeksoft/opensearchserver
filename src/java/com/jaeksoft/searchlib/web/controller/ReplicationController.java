/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.replication.ReplicationItem;

public class ReplicationController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6081443889284004102L;

	private ReplicationItem selectedItem;

	private ReplicationItem item;

	public ReplicationController() throws SearchLibException, NamingException {
		super();
		selectedItem = null;
		item = new ReplicationItem();
	}

	public ReplicationItem getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(ReplicationItem item) {
		selectedItem = item;
	}

	public ReplicationItem getItem() {
		return item;
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

	@Override
	public void reset() {
	}

	public void onSave() throws InterruptedException, SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		if (selectedItem != null)
			selectedItem.copy(item);
		else
			client.getReplicationList().put(item);
		client.saveReplicationList();
		onCancel();
	}

	public void onCancel() {
		item = new ReplicationItem();
		selectedItem = null;
		reloadPage();
	}

	public void onDelete() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		Client client = getClient();
		client.getReplicationList().remove(selectedItem);
		client.saveReplicationList();
		onCancel();
	}

}
