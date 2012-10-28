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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.database.FileItem;
import com.jaeksoft.searchlib.crawler.file.database.FileManager;
import com.jaeksoft.searchlib.crawler.file.database.FileManager.SearchTemplate;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskFileManagerAction;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class FileController extends CrawlerController implements AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2429849843906661914L;

	private transient List<FileItem> fileList;

	private transient int totalSize;

	private transient int activePage;

	public FileController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		fileList = null;
		totalSize = 0;
		activePage = 0;
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) throws SearchLibException {
				onPaging((PagingEvent) event);
			}
		});
	}

	public FileManager getFileManager() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getFileManager();
		}
	}

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setLang(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_LANG.set(this, v);
		}
	}

	public String getLang() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_FILE_LANG.get(this);
		}
	}

	public void setExtension(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_FILE_EXTENSION.set(this, v);
		}
	}

	public String getExtension() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_FILE_FILE_EXTENSION.get(this);
		}
	}

	public void setMinContentLength(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_MIN_CONTENT_LENGTH.set(this, v);
		}
	}

	public Integer getMinContentLength() {
		synchronized (this) {
			return (Integer) ScopeAttribute.SEARCH_FILE_MIN_CONTENT_LENGTH
					.get(this);
		}
	}

	public void setMaxContentLength(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_MAX_CONTENT_LENGTH.set(this, v);
		}
	}

	public Integer getMaxContentLength() {
		synchronized (this) {
			return (Integer) ScopeAttribute.SEARCH_FILE_MAX_CONTENT_LENGTH
					.get(this);
		}
	}

	public void setFetchStatus(FetchStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_FETCH_STATUS.set(this, v);
		}
	}

	public FetchStatus getFetchStatus() {
		synchronized (this) {
			FetchStatus st = (FetchStatus) ScopeAttribute.SEARCH_FILE_FETCH_STATUS
					.get(this);
			if (st == null)
				return FetchStatus.ALL;
			return st;
		}
	}

	public void setParserStatus(ParserStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_PARSER_STATUS.set(this, v);
		}
	}

	public ParserStatus getParserStatus() {
		synchronized (this) {
			ParserStatus status = (ParserStatus) ScopeAttribute.SEARCH_FILE_PARSER_STATUS
					.get(this);
			if (status == null)
				return ParserStatus.ALL;
			return status;
		}
	}

	public void setIndexStatus(IndexStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_INDEX_STATUS.set(this, v);
		}
	}

	public IndexStatus getIndexStatus() {
		synchronized (this) {
			IndexStatus status = (IndexStatus) ScopeAttribute.SEARCH_FILE_INDEX_STATUS
					.get(this);
			if (status == null)
				return IndexStatus.ALL;
			return status;
		}
	}

	public void setFileType(FileTypeEnum v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_FILE_TYPE.set(this, v);
			setAttribute("searchUrlFileType", v, SESSION_SCOPE);
		}
	}

	public FileTypeEnum getFileType() {
		synchronized (this) {
			FileTypeEnum type = (FileTypeEnum) ScopeAttribute.SEARCH_FILE_FILE_TYPE
					.get(this);
			if (type == null)
				return FileTypeEnum.ALL;
			return type;
		}
	}

	public void setFileName(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_FILE_NAME.set(this, v);
		}
	}

	public String getFileName() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_FILE_FILE_NAME.get(this);
		}
	}

	public void setPageSize(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_SHEET_ROWS.set(this, v);
		}
	}

	public Integer getPageSize() {
		synchronized (this) {
			Integer v = (Integer) ScopeAttribute.SEARCH_FILE_SHEET_ROWS
					.get(this);
			if (v == null)
				v = 10;
			return v;
		}
	}

	public void setCrawlDateStart(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_DATE_START.set(this, v);
		}
	}

	public Date getCrawlDateStart() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_FILE_DATE_START.get(this);
		}
	}

	public void setCrawlDateEnd(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_DATE_END.set(this, v);
		}
	}

	public Date getCrawlDateEnd() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_FILE_DATE_END.get(this);
		}
	}

	public void setDateModifiedStart(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_START.set(this, v);
		}
	}

	public Date getDateModifiedStart() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_START
					.get(this);
		}
	}

	public void setDateModifiedEnd(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_END.set(this, v);
		}
	}

	public Date getDateModifiedEnd() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_END
					.get(this);
		}
	}

	public void setRepository(String v) {
		synchronized (this) {
			if (v != null)
				if (v.length() == 0)
					v = null;
			ScopeAttribute.SEARCH_FILE_REPOSITORY.set(this, v);
		}
	}

	public String getRepository() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_FILE_REPOSITORY.get(this);
		}
	}

	public void onPaging(PagingEvent pagingEvent) throws SearchLibException {
		synchronized (this) {
			fileList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() throws SearchLibException {
		synchronized (this) {
			fileList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	@Override
	public void onReload() throws SearchLibException {
		synchronized (this) {
			reloadPage();
		}
	}

	private SearchRequest getSearchRequest(FileManager fileManager,
			SearchTemplate fileSearchTemplate) throws SearchLibException {
		return fileManager.fileQuery(fileSearchTemplate, getRepository(),
				getFileName(), getLang(), null, getMinContentLength(),
				getMaxContentLength(), getExtension(), getFetchStatus(),
				getParserStatus(), getIndexStatus(), getCrawlDateStart(),
				getCrawlDateEnd(), getDateModifiedStart(),
				getDateModifiedEnd(), getFileType(), null);
	}

	public List<FileItem> getFileList() throws SearchLibException {
		synchronized (this) {
			if (fileList != null)
				return fileList;

			FileManager fileManager = getFileManager();
			if (fileManager == null)
				return null;

			fileList = new ArrayList<FileItem>();
			SearchRequest searchRequest = getSearchRequest(fileManager,
					SearchTemplate.fileSearch);

			totalSize = (int) fileManager.getFiles(searchRequest,
					fileManager.getFileItemFieldEnum().uri, true, getPageSize()
							* getActivePage(), getPageSize(), fileList);
			return fileList;
		}
	}

	public FetchStatus[] getFetchStatusList() {
		synchronized (this) {
			return FetchStatus.values();
		}
	}

	public RobotsTxtStatus[] getRobotsTxtStatusList() {
		synchronized (this) {
			return RobotsTxtStatus.values();
		}
	}

	public ParserStatus[] getParserStatusList() {
		synchronized (this) {
			return ParserStatus.values();
		}
	}

	public IndexStatus[] getIndexStatusList() {
		synchronized (this) {
			return IndexStatus.values();
		}
	}

	public FileTypeEnum[] getFileTypeList() {
		synchronized (this) {
			return FileTypeEnum.values();
		}
	}

	public List<String> getRepositoryList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<String> list = new ArrayList<String>();
			list.add("");
			client.getFilePathManager().getAllFilePathsString(list);
			return list;
		}
	}

	private void onTask(TaskFileManagerAction taskFileManagerAction)
			throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		TaskItem taskItem = new TaskItem(client, taskFileManagerAction);
		TaskManager.executeTask(client, taskItem, null);
		client.getFileManager().waitForTask(taskFileManagerAction, 30);
	}

	public void onSetToUnfetched() throws SearchLibException,
			InterruptedException {
		synchronized (this) {
			FileManager fileManager = getFileManager();
			if (fileManager == null)
				return;
			SearchRequest searchRequest = getSearchRequest(fileManager,
					SearchTemplate.fileSearch);
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setSelection(searchRequest, false, true);
			taskFileManagerAction.setOptimize();
			onTask(taskFileManagerAction);
		}
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		synchronized (this) {
			FileManager fileManager = getFileManager();
			if (fileManager == null)
				return;
			SearchRequest searchRequest = getSearchRequest(fileManager,
					SearchTemplate.fileExport);
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setSelection(searchRequest, true, false);
			taskFileManagerAction.setOptimize();
			onTask(taskFileManagerAction);
		}
	}

	public void onOptimize() throws SearchLibException, InterruptedException {
		synchronized (this) {
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setOptimize();
			onTask(taskFileManagerAction);
		}
	}

	public void onGo() throws SearchLibException, IOException,
			TransformerConfigurationException, SAXException,
			InterruptedException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			if (client.getFileCrawlMaster().isRunning()) {
				new AlertController("Please stop the File crawler first.");
				return;
			}
			Listbox actionListbox = (Listbox) getFellow("actionListbox");
			String action = actionListbox.getSelectedItem().getValue()
					.toString();
			if ("setToUnfetched".equalsIgnoreCase(action))
				onSetToUnfetched();
			else if ("delete".equalsIgnoreCase(action))
				onDelete();
			else if ("optimize".equalsIgnoreCase(action))
				onOptimize();
		}
	}

	public void onTimer() {
		reloadComponent("taskLogInfo");
	}

	public boolean isRefresh() throws SearchLibException {
		FileManager fileManager = getFileManager();
		if (fileManager == null)
			return false;
		return fileManager.isCurrentTaskLog();
	}
}
