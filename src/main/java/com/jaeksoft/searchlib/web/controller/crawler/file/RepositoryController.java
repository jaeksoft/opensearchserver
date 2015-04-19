/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;

@AfterCompose(superclass = true)
public class RepositoryController extends FileCrawlerController {

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	public RepositoryController() throws SearchLibException {
		super();
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
	}

	public void setPageSize(int v) {
		pageSize = v;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setActivePage(int page) throws SearchLibException {
		synchronized (this) {
			activePage = page;
			reload();
		}
	}

	public FilePathItem getSelectedFilePathItem() {
		return null;
	}

	@NotifyChange("*")
	public void setSelectedFilePathItem(FilePathItem selectedFilePathItem)
			throws SearchLibException, URISyntaxException {
		if (selectedFilePathItem == null)
			return;
		setFilePathItemSelected(selectedFilePathItem);
		FilePathItem editFilePath = new FilePathItem(getClient());
		selectedFilePathItem.copyTo(editFilePath);
		setFilePathItemEdit(editFilePath);
	}

	@Command
	@NotifyChange("*")
	public void onNewFilePathItem() throws SearchLibException {
		setFilePathItemEdit(new FilePathItem(getClient()));
		setFilePathItemSelected(null);
	}

	public List<FilePathItem> getFilePathItemList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;

			List<FilePathItem> filePathItemList = new ArrayList<FilePathItem>();

			totalSize = client.getFilePathManager().getFilePaths(
					getActivePage() * getPageSize(), getPageSize(),
					filePathItemList);

			return filePathItemList;
		}
	}

}