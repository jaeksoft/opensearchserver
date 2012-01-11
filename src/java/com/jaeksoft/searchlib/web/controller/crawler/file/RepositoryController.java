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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Tab;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class RepositoryController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1207354816338087824L;

	private transient List<FilePathItem> filePathItemList = null;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	public RepositoryController() throws SearchLibException {
		super();
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) {
				onPaging((PagingEvent) event);
			}
		});
	}

	@Override
	public void eventFilePathEdit(FilePathItem filePathItem)
			throws SearchLibException {
		onRefresh();
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

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			filePathItemList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	@Override
	protected void reset() {
		filePathItemList = null;
	}

	public void onRefresh() {
		reset();
		reloadPage();
	}

	public FilePathItem getSelectedFilePathItem() {
		return null;
	}

	public void setSelectedFilePathItem(FilePathItem selectedFilePathItem) {
		if (selectedFilePathItem == null)
			return;
		reloadPage();
		PushEvent.FILEPATH_EDIT.publish(selectedFilePathItem);
		Tab tab = (Tab) getFellow("tabCrawlerFileEdit", true);
		tab.setSelected(true);
	}

	public List<FilePathItem> getFilePathItemList() throws SearchLibException {
		synchronized (this) {
			if (filePathItemList != null)
				return filePathItemList;
			Client client = getClient();
			if (client == null)
				return null;

			filePathItemList = new ArrayList<FilePathItem>();

			totalSize = client.getFilePathManager().getFilePaths(
					getActivePage() * getPageSize(), getPageSize(),
					filePathItemList);

			return filePathItemList;
		}
	}

}