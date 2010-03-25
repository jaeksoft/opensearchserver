/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Listbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class BrowserController extends CrawlerController implements
		AfterCompose {

	private static final long serialVersionUID = 6735801464584819587L;

	private List<FilePathItem> filePathItemList = null;

	private File[] currentFileList;

	private List<File> folderTree;

	private File currentFile;

	private File selectedFile;

	private boolean withSubDir;

	private FilePathItem selectedFilePathItem;

	public BrowserController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		filePathItemList = null;
		currentFileList = null;
		folderTree = new ArrayList<File>(0);
		currentFile = null;
		selectedFile = null;
		withSubDir = false;
		selectedFilePathItem = null;
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
		withSubDir = false;
	}

	public void setWithSubDir(boolean b) {
		this.withSubDir = b;
	}

	public boolean isWithSubDir() {
		return withSubDir;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		synchronized (this) {
			this.currentFile = currentFile;
			folderTree.clear();
			File parent = currentFile;
			while (parent != null) {
				folderTree.add(0, parent);
				parent = parent.getParentFile();
			}
			setSelectedFile(null);
			refreshCurrentFileList();
		}
	}

	public boolean isNoSelectedFile() {
		synchronized (this) {
			return getSelectedFile() == null;
		}
	}

	public boolean isNoSelectedDir() {
		synchronized (this) {
			if (selectedFile == null)
				return true;
			return !selectedFile.isDirectory();
		}
	}

	protected void refreshCurrentFileList() {
		synchronized (this) {
			if (currentFile == null) {
				currentFileList = File.listRoots();
			} else
				currentFileList = currentFile.listFiles();
		}
	}

	public File[] getCurrentFileList() {
		synchronized (this) {
			if (currentFileList == null)
				refreshCurrentFileList();
			return currentFileList;
		}
	}

	public List<File> getFolderTree() {
		synchronized (this) {
			return folderTree;
		}
	}

	public FilePathItem getSelectedFilePathItem() {
		return selectedFilePathItem;
	}

	public void setSelectedFilePathItem(FilePathItem selectedFilePathItem) {
		this.selectedFilePathItem = selectedFilePathItem;
	}

	public List<FilePathItem> getFilePathItemList() throws SearchLibException {
		synchronized (this) {
			if (filePathItemList != null)
				return filePathItemList;
			Client client = getClient();
			if (client == null)
				return null;

			filePathItemList = new ArrayList<FilePathItem>();

			client.getFilePathManager().getFilePaths(null, 0, 1000000,
					filePathItemList);

			return filePathItemList;
		}
	}

	public boolean isRoot() {
		return currentFile == null;
	}

	public boolean isNotRoot() {
		return !isRoot();
	}

	public void onAdd() throws SearchLibException,
			UnsupportedEncodingException, ParseException, InterruptedException {
		synchronized (this) {
			if (selectedFile == null)
				return;
			Client client = getClient();
			if (client == null)
				return;
			FilePathManager filePathManager = client.getFilePathManager();
			if (filePathManager == null)
				return;
			if (filePathManager.getFilePath(selectedFile) != null) {
				Messagebox.show("The file or directory is already selected.",
						"OpenSearchServer", Messagebox.OK,
						org.zkoss.zul.Messagebox.INFORMATION);
				return;
			}
			filePathManager.add(selectedFile, withSubDir);
			filePathItemList = null;
			reloadPage();
		}
	}

	public void onRemoveFilePath(Component component) throws SearchLibException {
		synchronized (this) {
			FilePathItem filePathItem = (FilePathItem) component
					.getAttribute("filepath");

			Client client = getClient();
			if (client == null)
				return;
			FilePathManager filePathManager = client.getFilePathManager();
			if (filePathManager == null)
				return;
			filePathManager.remove(filePathItem.getFilePath());
			filePathItemList = null;
			reloadPage();
		}
	}

	public void onOpenFile(Component component) throws SearchLibException {
		synchronized (this) {
			File file = (File) component.getAttribute("file");
			if (file.isDirectory()) {
				setCurrentFile(file);
				reloadPage();
			}

		}
	}

	public void onParentFolder() throws SearchLibException {
		synchronized (this) {
			if (currentFile == null)
				return;
			setCurrentFile(currentFile.getParentFile());
			setSelectedFile(null);
			reloadPage();
		}
	}

	public void onRefresh() {
		synchronized (this) {
			refreshCurrentFileList();
			reloadPage();
		}
	}

	public void onSelectFilePathItem() {
		if (selectedFilePathItem == null)
			return;
		File f = selectedFilePathItem.getFilePath();
		if (f == null)
			return;
		setCurrentFile(f.getParentFile());
		setSelectedFile(f);
		reloadPage();
	}

	private void invalidateFileBrowserListBox() {
		Listbox listbox = (Listbox) getFellow("filebrowser");
		listbox.invalidate();

	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			invalidateFileBrowserListBox();
			super.reloadPage();
		}
	}

}