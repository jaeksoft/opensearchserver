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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.dropbox.core.DbxWebAuth;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.DropboxFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SmbFileInstance.SmbSecurityPermissions;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.web.controller.AlertController;

@AfterCompose(superclass = true)
public class FilePathEditController extends FileCrawlerController {

	private FilePathItem currentFilePath;

	private boolean showHidden;

	private DbxWebAuth webAuthInfo;

	private boolean pathIsValid = false;

	private class DeleteAlert extends AlertController {

		private FilePathItem deleteFilePath;

		protected DeleteAlert(FilePathItem deleteFilePath) throws InterruptedException {
			super("Please, confirm that you want to delete the location: " + deleteFilePath.toString(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.deleteFilePath = deleteFilePath;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getFileManager().deleteByRepository(deleteFilePath.toString());
			client.getFilePathManager().remove(deleteFilePath);
			onCancel();
		}
	}

	public FilePathEditController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
		currentFilePath = null;
		pathIsValid = false;
		showHidden = false;
		webAuthInfo = null;
	}

	@Override
	public void reload() throws SearchLibException {
		currentFilePath = getFilePathItemEdit();
		if (currentFilePath != null)
			checkPath(currentFilePath.getPath());
		super.reload();
	}

	public FileInstanceType[] getTypeList() throws SearchLibException {
		return FileInstanceType.values();
	}

	/**
	 * 
	 * @return the current FilePathItem
	 */
	public FilePathItem getCurrentFilePath() {
		return currentFilePath;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return isNoFilePathSelected() ? "Add a new location" : "Edit the selected location";
	}

	@Command
	@NotifyChange("pathValid")
	public void checkPath(@BindingParam("path") String path) {
		pathIsValid = false;
		if (StringUtils.isEmpty(path))
			return;
		if (isLocalFileType())
			pathIsValid = new File(path).exists();
	}

	public boolean isPathValid() {
		return pathIsValid;
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
		setFilePathItemEdit(null);
		reload();
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		FilePathItem filePath = getFilePathItemSelected();
		if (filePath == null)
			return;
		new DeleteAlert(filePath);
	}

	@Command
	public void onCheck() throws InterruptedException, InstantiationException, IllegalAccessException,
			URISyntaxException, IOException {
		if (currentFilePath == null)
			return;
		new AlertController("Test results: " + currentFilePath.check());
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException, URISyntaxException {
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

	public boolean isLocalFileType() {
		if (currentFilePath == null)
			return false;
		return "file".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotLocalFileType() {
		return !isLocalFileType();
	}

	public boolean isSwiftFileType() {
		if (currentFilePath == null)
			return false;
		return "swift".equals(currentFilePath.getType().getScheme());
	}

	public boolean isSmbFileType() {
		if (currentFilePath == null)
			return false;
		return "smb".equals(currentFilePath.getType().getScheme());
	}

	public boolean isFtpFileType() {
		if (currentFilePath == null)
			return false;
		String scheme = currentFilePath.getType().getScheme();
		return "ftp".equals(scheme) || "ftps".equals(scheme);
	}

	public boolean isNotSwiftFileType() {
		return !isSwiftFileType();
	}

	public AuthType[] getSwiftAuthTypes() {
		return AuthType.values();
	}

	public SmbSecurityPermissions[] getSmbSecurityPermissions() {
		return SmbSecurityPermissions.values();
	}

	public boolean isDomain() {
		if (currentFilePath == null)
			return false;
		return "smb".equals(currentFilePath.getType().getScheme());
	}

	public FileInstanceType getCurrentFileType() {
		if (currentFilePath == null)
			return null;
		return currentFilePath.getType();
	}

	public void setCurrentFileType(FileInstanceType type) throws SearchLibException {
		currentFilePath.setType(type);
		reload();
	}

	public boolean isShowHidden() {
		return showHidden;
	}

	public void setShowHidden(boolean b) throws SearchLibException {
		showHidden = b;
		reload();
	}

	public boolean isDropbox() {
		if (currentFilePath == null)
			return false;
		return currentFilePath.getType().is(DropboxFileInstance.class);
	}

	@Command
	public void onDropboxAuthRequest() throws MalformedURLException, SearchLibException {
		webAuthInfo = DropboxFileInstance.requestAuthorization();
		reload();
		throw new SearchLibException("Not yet implemented");
		// Executions.getCurrent().sendRedirect(null, "_blank");
	}

	@Command
	public void onDropboxConfirmAuth() throws SearchLibException, InterruptedException {
		StringBuilder uid = new StringBuilder();
		String atp = DropboxFileInstance.retrieveAccessToken(webAuthInfo, uid);
		if (uid.length() == 0) {
			new AlertController("The Dropbox authentication process failed");
			return;
		}
		currentFilePath.setHost(uid.toString() + ".dropbox.com");
		currentFilePath.setUsername("");
		currentFilePath.setPassword(atp);
		reload();
	}

	public boolean isDropboxWebAuthInfo() {
		return webAuthInfo != null;
	}

	public boolean isNotDropboxWebAuthInfo() {
		return !isDropboxWebAuthInfo();
	}

	public String getDropboxAuthUrl() throws SearchLibException {
		if (webAuthInfo == null)
			return null;
		throw new SearchLibException("Not yet implemented");
	}

}
