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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.SAXException;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.util.GenericLink;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class BrowserController extends CrawlerController implements
		ListitemRenderer, AfterCompose {

	private static final long serialVersionUID = 6735801464584819587L;

	private final String ROOT = "..";

	transient private List<FilePathItem> pathList = null;

	transient private List<File> files;

	private int pageSize;
	private int totalSize;
	private int activePage;

	private File currentFile;
	private File selectedFile;
	private File selectedFilePath;
	private boolean selectedFileCheck;

	public BrowserController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		pathList = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		files = null;
		currentFile = null;
		selectedFile = null;
		selectedFilePath = null;
		selectedFileCheck = false;
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

	public boolean isSelectedFileCheck() {
		if (getSelectedFile() != null && getSelectedFile().isFile())
			selectedFileCheck = false;
		return selectedFileCheck;
	}

	public void setSelectedFileCheck(boolean selectedFileCheck) {
		if (getSelectedFile() != null && getSelectedFile().isFile())
			this.selectedFileCheck = false;
		else
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

	public File getSelectedFilePath() {
		return selectedFilePath;
	}

	public void setSelectedFilePath(File selectedFilePath) {
		this.selectedFilePath = selectedFilePath;

		if (selectedFilePath != null) {
			if (selectedFilePath.equals(ROOT))
				selectedFile = new File("");
			else
				selectedFile = new File(selectedFilePath.getAbsolutePath());
		} else
			selectedFile = null;
	}

	public List<File> getFiles() {
		synchronized (this) {
			if (files != null)
				return files;

			files = null;

			File[] dataToInsert = null;
			if (currentFile == null) {
				dataToInsert = File.listRoots();
			} else {
				dataToInsert = currentFile.listFiles();
			}

			List<File> onlyFile = new ArrayList<File>();
			List<File> onlyDir = new ArrayList<File>();

			if (dataToInsert != null && dataToInsert.length > 0) {
				for (int i = 0; i < dataToInsert.length; i++) {
					File current = dataToInsert[i];
					if (current.isDirectory())
						onlyDir.add(current);
					else
						onlyFile.add(current);
				}
				files = new ArrayList<File>();
				files.addAll(onlyDir);
				files.addAll(onlyFile);
			}

			return files;
		}
	}

	public FilePathItem getFilePathItem() {
		synchronized (this) {
			try {
				Client client = getClient();
				if (client == null)
					return null;

				pathList = new ArrayList<FilePathItem>();

				totalSize = client.getFilePathManager().getFilePaths(null,
						getActivePage() * getPageSize(), getPageSize(),
						pathList);

				return null;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private boolean isRoot() {
		if (currentFile == null)
			return false;

		for (File file : File.listRoots()) {
			if (file.getPath().equals(currentFile.getPath()))
				return true;
		}

		return false;
	}

	/**
	 * Event methods
	 */

	@SuppressWarnings("unchecked")
	@Override
	public void render(Listitem item, Object data) throws Exception {

		if (data instanceof GenericLink) {
			GenericLink<String> link = (GenericLink<String>) data;
			new Listcell(link.getSource()).setParent(item);
			new Listcell(link.getTarget()).setParent(item);

			Listcell listcellRest = new Listcell();
			Image imageValid = new Image("/images/action_valid.png");
			imageValid.addForward(null, this, "onResetChosen", data);
			imageValid.setParent(listcellRest);
			listcellRest.setParent(item);

			Listcell listcell = new Listcell();
			Image image = new Image("/images/action_delete.png");
			image.addForward(null, this, "onRemove", data);
			image.setParent(listcell);
			listcell.setParent(item);

		} else if (data instanceof String) {
			new Listcell((String) data).setParent(item);
			Listcell listcell = new Listcell();
			listcell.setParent(item);
		}
	}

	@SuppressWarnings("unchecked")
	public void onRemove(Event event) throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		GenericLink<String> link = (GenericLink<String>) event.getData();
		synchronized (link) {
			getClient().getFilePathManager().delPath(link.getSource());
			deleteOriginalPath(link.getSource());
		}
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	public void onResetChosen(Event event) throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		GenericLink<String> link = (GenericLink<String>) event.getData();
		synchronized (link) {
			deleteOriginalPath(link.getSource());
		}
	}

	public void onAdd() throws SearchLibException,
			UnsupportedEncodingException, ParseException, InterruptedException {
		synchronized (this) {
			if (getSelectedFile() != null) {
				// Already In
				if (getClient().getFilePathManager().getFilePaths(
						getSelectedFile().getPath(), 0, 0, null) > 0) {
					Messagebox
							.show("Already In.", "Jaeksoft OpenSearchServer",
									Messagebox.OK,
									org.zkoss.zul.Messagebox.INFORMATION);
				} else {
					/*
					 * List<FilePathItem> list = FilePathManager(
					 * getSelectedFile().getPath(), getSelectedFile()
					 * .isDirectory() && isSelectedFileCheck());
					 * 
					 * if (list.size() > 0) {
					 * getClient().getFilePathManager().addList(list, false); }
					 */
					pathList = null;
					setSelectedFileCheck(false);
					setSelectedFilePath(null);
					reloadPage();
				}
			}
		}
	}

	public void onDoubleClick(Listcell cell) throws SearchLibException {
		synchronized (this) {
			String path = cell.getLabel();
			if (path == null)
				return;

			File currentFile = new File(path);
			if (currentFile != null && currentFile.isDirectory()) {
				setCurrentFile(currentFile);
				setSelectedFile(null);
				reloadPage();
			}

		}
	}

	public void onBack() throws SearchLibException {
		synchronized (this) {
			if (currentFile != null && !isRoot())
				setCurrentFile(currentFile.getParentFile());
			else
				setCurrentFile(null);

			setSelectedFile(null);
			reloadPage();
		}
	}

	public void onRefresh() throws SearchLibException {
		synchronized (this) {
			reloadPage();
		}
	}

	@Override
	public void afterCompose() {
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			public void onEvent(Event event) {
				onPaging((PagingEvent) event);
			}
		});
	}

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			pathList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	private void deleteOriginalPath(String path) throws SearchLibException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {

		URI uri = (new File(path)).toURI();
		CrawlStatus status = getClient().getFileCrawlMaster().getStatus();

		if (status.equals(CrawlStatus.NOT_RUNNING)
				|| status.equals(CrawlStatus.ABORTED)) {
			List<String> listUri = new ArrayList<String>();
			listUri.add(uri.toASCIIString());
			getClient().getFileManager().deleteByOriginalUri(listUri);
			getClient().getFileManager().reload(true);
		} else {
			getClient().getFileCrawlMaster().deleteToCrawlQueue(uri);
		}
	}

}