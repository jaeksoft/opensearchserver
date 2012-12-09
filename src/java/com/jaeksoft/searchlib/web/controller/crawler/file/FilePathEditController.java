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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Messagebox;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.DropboxFileInstance;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.controller.AlertController;

public class FilePathEditController extends FileCrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -46755671370102218L;

	private FilePathItem currentFilePath;

	private transient FileSelectorItem currentFile;

	private transient FileSelectorItem currentFolder;

	private transient List<FileSelectorItem> currentFolderList;

	private transient FileSelectorItem[] currentFileList;

	private boolean showHidden;

	private WebAuthInfo webAuthInfo;

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

	public class FileSelectorItem {

		final protected File file;

		protected FileSelectorItem(File file) {
			this.file = file;
		}

		public String getName() {
			if (file == null)
				return null;
			String n = file.getName();
			if (n != null && n.length() > 0)
				return n;
			return file.getPath();
		}

		public File getFile() {
			return file;
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
		currentFilePath = null;
		currentFileList = null;
		currentFile = null;
		currentFolder = null;
		showHidden = false;
		webAuthInfo = null;
	}

	public List<FileInstanceType> getTypeList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getFileManager().getFileTypeEnum().getList();
	}

	@Override
	public void reloadPage() throws SearchLibException {
		FilePathItem filePathItem = getFilePathItemEdit();
		if (filePathItem == currentFilePath || filePathItem == null) {
			super.reloadPage();
			return;
		}
		try {
			currentFilePath = filePathItem;
			if ("file".equals(filePathItem.getType().getScheme())) {
				String path = filePathItem.getPath();
				if (path != null) {
					File f = new File(path);
					if (f.exists()) {
						setCurrentFolder(f.getParentFile());
						setCurrentFile(new FileSelectorItem(new File(path)));
					}
				}
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		super.reloadPage();
	}

	/**
	 * 
	 * @return the current FilePathItem
	 */
	public FilePathItem getCurrentFilePath() {
		return currentFilePath;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return isNoFilePathSelected() ? "Add a new location"
				: "Edit the selected location";
	}

	public void onCancel() throws SearchLibException {
		reset();
		setFilePathItemEdit(null);
		reloadFileCrawlerPages();
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		FilePathItem filePath = getFilePathItemSelected();
		if (filePath == null)
			return;
		new DeleteAlert(filePath);
	}

	public void onSave() throws InterruptedException, SearchLibException,
			URISyntaxException {
		Client client = getClient();
		if (client == null)
			return;
		FilePathManager filePathManager = client.getFilePathManager();
		FilePathItem checkFilePath = filePathManager.get(currentFilePath);
		FilePathItem selectedFilePath = getFilePathItemSelected();
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

	private FileSelectorItem[] getList(File[] files) {
		if (files == null)
			return null;
		FileSelectorItem[] items = new FileSelectorItem[files.length];
		int i = 0;
		for (File file : files)
			items[i++] = new FileSelectorItem(file);
		return items;

	}

	public FileSelectorItem[] getCurrentFileList() throws SearchLibException,
			IOException {
		synchronized (this) {
			if (currentFileList != null)
				return currentFileList;
			File[] files = null;
			getCurrentFolder();
			if (currentFolder == null) {
				files = File.listRoots();
			} else {
				if (!isShowHidden())
					files = currentFolder.file
							.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
				else
					files = currentFolder.file.listFiles();
			}
			currentFileList = getList(files);
			return currentFileList;
		}
	}

	public void setCurrentFile(FileSelectorItem item) {
		currentFile = item;
		reloadBrowser();
	}

	public FileSelectorItem getCurrentFile() {
		return currentFile;
	}

	public FileSelectorItem getCurrentFolder() throws SearchLibException,
			IOException {
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

	public List<FileSelectorItem> getFolderTree() {
		return currentFolderList;
	}

	public boolean isNotRoot() {
		return currentFolder != null;
	}

	public boolean isLocalFileType() {
		if (currentFilePath == null)
			return false;
		return "file".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotLocalFileType() {
		return !isLocalFileType();
	}

	public boolean isDomain() {
		if (currentFilePath == null)
			return false;
		return "smb".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotSelectedFile() {
		return currentFile != null;
	}

	public boolean isSelectedFile() {
		return !isNotSelectedFile();
	}

	public void setCurrentFolder(FileSelectorItem fileSelectorItem)
			throws IOException {
		if (fileSelectorItem != null)
			if (!ClientFactory.INSTANCE.properties
					.checkChrootQuietly(fileSelectorItem.file))
				return;
		currentFolder = fileSelectorItem;
		currentFolderList = null;
		if (currentFolder != null) {
			currentFolderList = new ArrayList<FileSelectorItem>();
			FileSelectorItem f = currentFolder;
			for (;;) {
				currentFolderList.add(0, new FileSelectorItem(f.file));
				File p = f.file.getParentFile();
				if (p == null)
					break;
				f = new FileSelectorItem(p);
				if (!ClientFactory.INSTANCE.properties
						.checkChrootQuietly(f.file))
					break;
			}
		}
		currentFileList = null;
		currentFile = null;
		reloadBrowser();
	}

	private void setCurrentFolder(File file) throws IOException {
		if (file == null)
			setCurrentFolder((FileSelectorItem) null);
		else
			setCurrentFolder(new FileSelectorItem(file));
	}

	public FileInstanceType getCurrentFileType() {
		if (currentFilePath == null)
			return null;
		return currentFilePath.getType();
	}

	public void setCurrentFileType(FileInstanceType type)
			throws SearchLibException {
		currentFilePath.setType(type);
		reloadPage();
	}

	public boolean isShowHidden() {
		return showHidden;
	}

	public void setShowHidden(boolean b) throws SearchLibException {
		showHidden = b;
		currentFileList = null;
		reloadBrowser();
	}

	public void reloadBrowser() {
		reloadComponent("filebrowser");
	}

	public void onOpenFile(Listcell cell) throws IOException {
		if (currentFile != null)
			if (currentFile.file.isDirectory())
				setCurrentFolder(currentFile);
	}

	public void onSelectFile() throws SearchLibException {
		if (currentFile != null) {
			currentFilePath.setPath(currentFile.file.getAbsolutePath());
			reloadPage();
		}
	}

	public void onRefreshList() {
		currentFileList = null;
		reloadBrowser();
	}
	
	public void onParentFolder() throws IOException {
		if (currentFolder != null)
			setCurrentFolder(currentFolder.file.getParentFile());
	}

	public boolean isDropbox() {
		if (currentFilePath == null)
			return false;
		return currentFilePath.getType().is(DropboxFileInstance.class);
	}

	public void onDropboxAuthRequest() throws MalformedURLException,
			SearchLibException {
		webAuthInfo = DropboxFileInstance.requestAuthorization();
		reloadPage();
		Executions.getCurrent().sendRedirect(webAuthInfo.url, "_blank");
	}

	public void onDropboxConfirmAuth() throws SearchLibException,
			InterruptedException {
		StringBuffer uid = new StringBuffer();
		AccessTokenPair atp = DropboxFileInstance.retrieveAccessToken(
				webAuthInfo, uid);
		if (uid.length() == 0) {
			new AlertController("The Dropbox authentication process failed");
			return;
		}
		currentFilePath.setHost(uid.toString() + ".dropbox.com");
		currentFilePath.setUsername(atp.key);
		currentFilePath.setPassword(atp.secret);
		reloadPage();
	}

	public boolean isDropboxWebAuthInfo() {
		return webAuthInfo != null;
	}

	public boolean isNotDropboxWebAuthInfo() {
		return !isDropboxWebAuthInfo();
	}

	public String getDropboxAuthUrl() {
		if (webAuthInfo == null)
			return null;
		return webAuthInfo.url;
	}

}
