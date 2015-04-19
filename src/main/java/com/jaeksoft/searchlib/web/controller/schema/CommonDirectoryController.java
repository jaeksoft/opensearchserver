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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public abstract class CommonDirectoryController<T> extends CommonController {

	private transient String editName;

	private transient T content;

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
			onCancel();
		}
	}

	public CommonDirectoryController() throws SearchLibException {
		super();
	}

	public abstract AbstractDirectoryManager<T> getManager()
			throws SearchLibException;

	@Command
	@NotifyChange("*")
	public void onAdd() throws IOException, SearchLibException,
			InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		AbstractDirectoryManager<?> manager = getManager();
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
	}

	@Command
	@NotifyChange("*")
	public void onEdit(@BindingParam("listname") String listname)
			throws SearchLibException, IOException {
		AbstractDirectoryManager<T> manager = getManager();
		if (manager == null)
			return;
		currentName = listname;
		content = manager.getContent(listname);
	}

	@Command
	public void onCancel() throws SearchLibException {
		currentName = null;
		content = null;
		reload();
	}

	@Override
	protected void reset() throws SearchLibException {
		editName = null;
		content = null;
		currentName = null;
	}

	@Command
	public void onSave() throws IOException, InterruptedException,
			SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		AbstractDirectoryManager<T> manager = getManager();
		if (manager == null)
			return;
		manager.saveContent(currentName, content);
		client.getSchema().recompileAnalyzers();
		client.reload();
		onCancel();
	}

	@Command
	public void onDelete(@BindingParam("listname") String listname)
			throws InterruptedException {
		new DeleteAlert(listname);
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
	public void setContent(T content) {
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public T getContent() {
		return content;
	}
}
