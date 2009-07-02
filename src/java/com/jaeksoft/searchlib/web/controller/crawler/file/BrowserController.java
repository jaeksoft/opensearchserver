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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.FileItem;
import com.jaeksoft.searchlib.crawler.web.database.FilePatternManager;
import com.jaeksoft.searchlib.crawler.web.database.FileSelector;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class BrowserController extends CommonController implements
		ListitemRenderer, FileSelector, AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6735801464584819587L;

	transient private List<FileItem> patternList = null;

	private int pageSize;
	private int totalSize;
	private int activePage;
	private final Set<String> selection;

	private File currentFile;
	private File selectedFile;
	private boolean selectedFileCheck;

	private String currentFilePath;

	public BrowserController() throws SearchLibException {
		super();
		patternList = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selection = new TreeSet<String>();
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

	public String getCurrentFilePath() {
		return currentFilePath;
	}

	public void setCurrentFilePath(String currentFilePath) {
		this.currentFilePath = currentFilePath;
	}

	public boolean isSelectedFileCheck() {
		return selectedFileCheck;
	}

	public void setSelectedFileCheck(boolean selectedFileCheck) {
		this.selectedFileCheck = selectedFileCheck;
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

	public File[] getFiles() {
		if (currentFile == null)
			return File.listRoots();
		else
			return currentFile.listFiles();

	}

	public boolean isMyRoot() {
		if (currentFile == null)
			return false;

		for (File file : File.listRoots()) {
			if (file.getPath().equals(currentFile.getPath()))
				return true;
		}

		return false;
	}

	public List<FileItem> getPatternList() {
		synchronized (this) {
			if (patternList != null)
				return patternList;
			try {
				FilePatternManager FilePatternManager = getClient()
						.getFilePatternManager();
				patternList = new ArrayList<FileItem>();

				totalSize = FilePatternManager.getPatterns("", getActivePage()
						* getPageSize(), getPageSize(), patternList);

				for (FileItem patternUrlItem : patternList)
					patternUrlItem.setFileSelector(this);
				return patternList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isSelection() {
		synchronized (this) {
			if (patternList == null)
				return false;
			return (getSelectionCount() > 0);
		}
	}

	@Override
	public void render(Listitem item, Object data) throws Exception {
		FileItem file = (FileItem) data;
		new Listcell(file.getPath());
		new Listcell(file.isWithSubToString());
		Listcell listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onLinkRemove", data);
		image.setParent(listcell);
		listcell.setParent(item);
	}

	public void addSelection(FileItem item) {
		synchronized (selection) {
			selection.add(item.getPath());
		}
	}

	public void removeSelection(FileItem item) {
		synchronized (selection) {
			selection.remove(item.getPath());
		}
	}

	public int getSelectionCount() {
		synchronized (selection) {
			return selection.size();
		}
	}

	public boolean isSelected(FileItem item) {
		synchronized (selection) {
			return selection.contains(item.getPath());
		}
	}

	public void deleteSelection(FilePatternManager FilePatternManager)
			throws SearchLibException {
		synchronized (selection) {
			FilePatternManager.delPattern(selection);
			selection.clear();
		}
	}

	public void onAdd() throws SearchLibException {

		synchronized (this) {
			if (getSelectedFile() != null) {
				List<FileItem> list = FilePatternManager.getPatternList(
						getSelectedFile().getPath(), isSelectedFileCheck());
				if (list.size() > 0) {
					getClient().getFilePatternManager().addList(list, false);
				}
				patternList = null;
				setSelectedFileCheck(false);
				reloadPage();
			}
		}
	}

	public void onIn() throws SearchLibException {
		synchronized (this) {
			if (getSelectedFile() != null)
				setCurrentFile(getSelectedFile());

			setSelectedFile(null);
			reloadPage();
		}
	}

	public void onReset() throws SearchLibException {
		synchronized (this) {
			setSelectedFile(null);
			setCurrentFile(null);
			// setFiles(null);
			reloadPage();
		}
	}

	public void onBack() throws SearchLibException {
		synchronized (this) {
			if (currentFile != null && !isMyRoot())
				setCurrentFile(currentFile.getParentFile());
			else
				setCurrentFile(null);

			setSelectedFile(null);
			reloadPage();
		}
	}

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			patternList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() {
		synchronized (this) {
			patternList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	public void onDelete() throws SearchLibException {
		synchronized (this) {
			FilePatternManager FilePatternManager = getClient()
					.getFilePatternManager();
			try {
				deleteSelection(FilePatternManager);
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
			onSearch();
			reloadPage();
		}
	}

	public void onSelect(Event event) {
		FileItem patternItem = (FileItem) event.getData();
		patternItem.setSelected(!patternItem.isSelected());
		reloadPage();
	}

	public void afterCompose() {
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			public void onEvent(Event event) {
				onPaging((PagingEvent) event);
			}
		});
	}

	/*
	 * public void onCheck(CheckEvent event) { synchronized (this) {
	 * System.out.println("lol" + event.isChecked());
	 * setSelectedFileCheck(event.isChecked()); } }
	 */
}