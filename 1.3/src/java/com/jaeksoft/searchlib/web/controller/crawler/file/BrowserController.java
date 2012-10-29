/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class BrowserController extends CrawlerController implements
		AfterCompose {

	private static final long serialVersionUID = 6735801464584819587L;

	private transient List<FilePathItem> filePathItemList = null;

	private transient File[] currentFileList;

	private transient List<File> folderTree;

	private transient File currentFile;

	private transient File selectedFile;

	private transient boolean withSubDir;

	private transient FilePathItem selectedFilePathItem;

	public BrowserController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
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
		((Button) getFellow("filebrowserbuttonadd"))
				.setDisabled(isNoSelectedFile());
		((Checkbox) getFellow("filebrowsercheckboxsubdir"))
				.setDisabled(isNoSelectedFile());
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

			client.getFilePathManager().getFilePaths(0, 1000000,
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

	public void onOpenFile(Component component) throws SearchLibException {
		synchronized (this) {
			File file = (File) component.getAttribute("file");
			if (file.isDirectory()) {
				setCurrentFile(file);
				invalidate("filebrowser");
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

	@Override
	public void doRefresh() throws SearchLibException {
		synchronized (this) {
			refreshCurrentFileList();
			reloadPage();
		}
	}

}