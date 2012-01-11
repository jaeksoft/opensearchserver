/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.PushEvent;

public class FilePathEditController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -46755671370102218L;

	private transient FilePathItem selectedFilePath;

	private transient FilePathItem currentFilePath;

	private transient File currentFile;

	private transient File currentFolder;

	private transient List<File> currentFolderList;

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
			client.getFileManager().deleteByRepository(
					deleteFilePath.toString());
			client.getFilePathManager().remove(deleteFilePath);
			onCancel();
		}
	}

	public FilePathEditController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		selectedFilePath = null;
		currentFilePath = new FilePathItem(client);
		currentFile = null;
		currentFolder = null;
	}

	public List<FileInstanceType> getTypeList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getFileManager().getFileTypeEnum().getList();
	}

	@Override
	public void eventFilePathEdit(FilePathItem filePathItem)
			throws SearchLibException {
		if (filePathItem == null)
			return;
		this.selectedFilePath = filePathItem;
		try {
			filePathItem.copyTo(currentFilePath);
			if ("file".equals(filePathItem.getType().getScheme())) {
				File f = new File(filePathItem.getPath());
				if (f.exists()) {
					setCurrentFolder(f.getParentFile());
					setCurrentFile(new File(filePathItem.getPath()));
				}
			}
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
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
		Tab tab = (Tab) getFellow("tabCrawlerRepository", true);
		tab.setSelected(true);
		PushEvent.FILEPATH_EDIT.publish();
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
		FilePathItem checkFilePath = filePathManager.get(currentFilePath);
		if (selectedFilePath == null) {
			if (checkFilePath != null) {
				new AlertController("The location already exists");
				return;
			}
		} else {
			if (checkFilePath != null)
				if (checkFilePath.hashCode() != selectedFilePath.hashCode()) {
					new AlertController("The location already exists");
					return;
				}
			filePathManager.remove(selectedFilePath);
		}
		filePathManager.add(currentFilePath);
		onCancel();
	}

	public File[] getCurrentFileList() throws SearchLibException, IOException {
		synchronized (this) {
			getCurrentFolder();
			if (currentFolder == null) {
				return File.listRoots();
			}
			if (isIgnoreHidden())
				return currentFolder
						.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
			else
				return currentFolder.listFiles();
		}
	}

	public void setCurrentFile(File file) {
		currentFile = file;
		reloadBrowser();
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public File getCurrentFolder() throws SearchLibException, IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (currentFolder == null
					&& ClientFactory.INSTANCE.properties.isChroot())
				setCurrentFolder(StartStopListener.OPENSEARCHSERVER_DATA_FILE);
			return currentFolder;
		}
	}

	public List<File> getFolderTree() {
		return currentFolderList;
	}

	public boolean isNotRoot() {
		return currentFolder != null;
	}

	public boolean isLocalFileType() {
		return "file".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotLocalFileType() {
		return !isLocalFileType();
	}

	public boolean isDomain() {
		return "smb".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotSelectedFile() {
		return currentFile != null;
	}

	public boolean isSelectedFile() {
		return !isNotSelectedFile();
	}

	public boolean isNotSelectedFilePath() {
		return !isSelectedFilePath();
	}

	public boolean isSelectedFilePath() {
		return selectedFilePath != null;
	}

	public void setCurrentFolder(File file) throws IOException {
		if (!ClientFactory.INSTANCE.properties.checkChrootQuietly(file))
			return;
		currentFolder = file;
		currentFolderList = null;
		if (currentFolder != null) {
			currentFolderList = new ArrayList<File>();
			File f = currentFolder;
			for (;;) {
				currentFolderList.add(0, f);
				f = f.getParentFile();
				if (f == null)
					break;
				if (!ClientFactory.INSTANCE.properties.checkChrootQuietly(f))
					break;
			}
		}
		currentFile = null;
		reloadBrowser();
	}

	public FileInstanceType getCurrentFileType() {
		return currentFilePath.getType();
	}

	public void setCurrentFileType(FileInstanceType type) {
		currentFilePath.setType(type);
		reloadPage();
	}

	public boolean isIgnoreHidden() {
		return currentFilePath.isIgnoreHidden();
	}

	public void setIgnoreHidden(boolean b) {
		currentFilePath.setIgnoreHidden(b);
		reloadPage();
	}

	public void reloadBrowser() {
		reloadComponent("filebrowser");
	}

	public void onOpenFile(Component component) throws IOException {
		File file = (File) component.getAttribute("file");
		if (file.isDirectory())
			setCurrentFolder(file);
	}

	public void onSelectFile() {
		if (currentFile != null) {
			currentFilePath.setPath(currentFile.getAbsolutePath());
			reloadPage();
		}
	}

	public void onParentFolder() throws IOException {
		if (currentFolder != null)
			setCurrentFolder(currentFolder.getParentFile());
	}

}
