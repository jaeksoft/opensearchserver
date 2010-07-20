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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public abstract class CommonDirectoryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8852402691915840607L;

	private String editName;

	private String content;

	protected class DeleteAlert extends AlertController {

		private String deleteListName;

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

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	public void onAdd() throws IOException, SearchLibException {
		getManager().create(getEditName());
		getClient().getSchema().recompileAnalyzers();
		getClient().reload();
		reloadPage();
	}

	public void onSave() throws IOException, InterruptedException,
			SearchLibException {
		getManager().saveContent(getEditName(), getContent());
		new AlertController("List saved!", Messagebox.INFORMATION);
		getClient().getSchema().recompileAnalyzers();
		getClient().reload();
		reloadPage();
	}

	public void onDelete(Component comp) throws InterruptedException {
		String name = (String) comp.getAttribute("listname");
		if (name == null)
			return;
		new DeleteAlert(name);
	}

	public boolean isEdit() throws SearchLibException {
		return getManager().exists(getEditName());
	}

	/**
	 * @param editName
	 *            the editName to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setEditName(String editName) throws SearchLibException,
			IOException {
		this.editName = editName;
		AbstractDirectoryManager manager = getManager();
		if (manager.exists(editName))
			setContent(manager.getContent(editName));
	}

	/**
	 * @return the editName
	 */
	public String getEditName() {
		return editName;
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
