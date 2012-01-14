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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public abstract class CommonDirectoryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8852402691915840607L;

	private transient String editName;

	private transient String content;

	private transient String currentName;

	protected class DeleteAlert extends AlertController {

		private transient String deleteListName;

		protected DeleteAlert(String listname) throws InterruptedException {
			super("Please, confirm that you want to delete the list: "
					+ listname, Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			deleteListName = listname;
		}

		@Override
		protected void onYes() throws SearchLibException {
			getManager().delete(deleteListName);
			getClient().getSchema().recompileAnalyzers();
			getClient().reload();
			reloadPage();
		}
	}

	public CommonDirectoryController() throws SearchLibException {
		super();
	}

	public abstract AbstractDirectoryManager getManager()
			throws SearchLibException;

	public void onAdd() throws IOException, SearchLibException,
			InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		AbstractDirectoryManager manager = getManager();
		if (manager == null)
			return;
		if (editName == null || editName.length() == 0) {
			new AlertController("Please enter a valid name", Messagebox.ERROR);
			return;
		}
		if (manager.exists(editName)) {
			new AlertController("This name already exists", Messagebox.ERROR);
			return;
		}
		currentName = editName;
		reloadPage();
	}

	public void onEdit(Component comp) throws SearchLibException, IOException {
		String name = (String) comp.getAttribute("listname");
		if (name == null)
			return;
		AbstractDirectoryManager manager = getManager();
		if (manager == null)
			return;
		currentName = name;
		content = manager.getContent(name);
		reloadPage();
	}

	public void onCancel() {
		currentName = null;
		content = null;
		reloadPage();
	}

	@Override
	protected void reset() throws SearchLibException {
		editName = null;
		content = null;
		currentName = null;
	}

	public void onSave() throws IOException, InterruptedException,
			SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		AbstractDirectoryManager manager = getManager();
		if (manager == null)
			return;
		if (!manager.exists(currentName))
			manager.create(currentName);
		manager.saveContent(currentName, content);
		client.getSchema().recompileAnalyzers();
		client.reload();
		onCancel();
	}

	public void onDelete(Component comp) throws InterruptedException {
		String name = (String) comp.getAttribute("listname");
		if (name == null)
			return;
		new DeleteAlert(name);
	}

	public boolean isEdit() {
		return currentName != null;
	}

	public boolean isNotEdit() {
		return !isEdit();
	}

	/**
	 * @param editName
	 *            the editName to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setEditName(String editName) {
		this.editName = editName;
	}

	/**
	 * @return the editName
	 */
	public String getEditName() {
		return editName;
	}

	/**
	 * 
	 * @return the currentName
	 */
	public String getCurrentName() {
		return currentName;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
}
