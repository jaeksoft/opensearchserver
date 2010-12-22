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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.net.URISyntaxException;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceEnum;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class FilePathEditController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -46755671370102218L;

	private FilePathItem selectedFilePath;

	private FilePathItem currentFilePath;

	private File currentFile;

	private File currentFolder;

	private class DeleteAlert extends AlertController {

		private FilePathItem deleteFilePath;

		protected DeleteAlert(FilePathItem deleteFilePath)
				throws InterruptedException {
			super("Please, confirm that you want to delete the location: "
					+ deleteFilePath.toString(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.deleteFilePath = deleteFilePath;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getFilePathManager().remove(deleteFilePath);
			onCancel();
		}
	}

	public FilePathEditController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFilePath = null;
		currentFilePath = new FilePathItem();
		currentFile = null;
		currentFolder = null;
	}

	public FileInstanceEnum[] getTypeList() {
		return FileInstanceEnum.values();
	}

	protected void eventFilePathEdit(FilePathItem filePathItem)
			throws SearchLibException, URISyntaxException {
		this.selectedFilePath = filePathItem;
		currentFilePath.copy(filePathItem);
		reloadPage();
	}

	/**
	 * 
	 * @return the current FilePathItem
	 */
	public FilePathItem getCurrentFilePath() {
		return currentFilePath;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedFilePath == null ? "Add a new location"
				: "Edit the selected location";
	}

	public boolean selected() {
		return selectedFilePath != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
		Tab tab = (Tab) getFellow("tabSchedulerList", true);
		tab.setSelected(true);
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		if (selectedFilePath == null)
			return;
		new DeleteAlert(selectedFilePath);
		onCancel();
	}

	public void onSave() throws InterruptedException, SearchLibException,
			URISyntaxException {
		Client client = getClient();
		if (client == null)
			return;
		FilePathManager filePathManager = client.getFilePathManager();
		if (selectedFilePath == null) {
			if (filePathManager.exists(currentFilePath)) {
				new AlertController("The location already exists");
				return;
			}
			filePathManager.add(currentFilePath);
		} else
			selectedFilePath.copy(currentFilePath);
		onCancel();
	}

	public File[] getCurrentFileList() {
		if (currentFolder == null)
			return File.listRoots();
		return currentFolder.listFiles();
	}

	public void setCurrentFile(File file) {
		currentFile = file;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public File getCurrentFolder() {
		return currentFolder;
	}

	public void setCurrentFolder(File file) {
		currentFolder = file;
		currentFile = null;
	}

	public void reloadBrowser() {
		getFellow("filebrowser").invalidate();
	}

	public void onOpenFile(Component component) {
		File file = (File) component.getAttribute("file");
		if (file.isDirectory())
			currentFolder = file;
		reloadBrowser();
	}

}
