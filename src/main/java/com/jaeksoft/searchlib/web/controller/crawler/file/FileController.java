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
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

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
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskFileManagerAction;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class FileController extends CrawlerController {

	private transient List<FileItem> fileList;

	private transient int totalSize;

	private transient int activePage;

	private transient String batchCommand;

	public FileController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		fileList = null;
		totalSize = 0;
		activePage = 0;
		batchCommand = null;
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

	public void setActivePage(int page) throws SearchLibException {
		synchronized (this) {
			fileList = null;
			activePage = page;
			reload();
		}
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setLang(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_LANG, v);
		}
	}

	public String getLang() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_FILE_LANG);
		}
	}

	public void setExtension(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_FILE_EXTENSION, v);
		}
	}

	public String getExtension() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_FILE_FILE_EXTENSION);
		}
	}

	public void setMinContentLength(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_MIN_CONTENT_LENGTH, v);
		}
	}

	public Integer getMinContentLength() {
		synchronized (this) {
			return (Integer) getAttribute(ScopeAttribute.SEARCH_FILE_MIN_CONTENT_LENGTH);
		}
	}

	public void setMaxContentLength(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_MAX_CONTENT_LENGTH, v);
		}
	}

	public Integer getMaxContentLength() {
		synchronized (this) {
			return (Integer) getAttribute(ScopeAttribute.SEARCH_FILE_MAX_CONTENT_LENGTH);
		}
	}

	public void setFetchStatus(FetchStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_FETCH_STATUS, v);
		}
	}

	public FetchStatus getFetchStatus() {
		synchronized (this) {
			FetchStatus st = (FetchStatus) getAttribute(ScopeAttribute.SEARCH_FILE_FETCH_STATUS);
			if (st == null)
				return FetchStatus.ALL;
			return st;
		}
	}

	public void setParserStatus(ParserStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_PARSER_STATUS, v);
		}
	}

	public ParserStatus getParserStatus() {
		synchronized (this) {
			ParserStatus status = (ParserStatus) getAttribute(ScopeAttribute.SEARCH_FILE_PARSER_STATUS);
			if (status == null)
				return ParserStatus.ALL;
			return status;
		}
	}

	public void setIndexStatus(IndexStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_INDEX_STATUS, v);
		}
	}

	public IndexStatus getIndexStatus() {
		synchronized (this) {
			IndexStatus status = (IndexStatus) getAttribute(ScopeAttribute.SEARCH_FILE_INDEX_STATUS);
			if (status == null)
				return IndexStatus.ALL;
			return status;
		}
	}

	public void setFileType(FileTypeEnum v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_FILE_TYPE, v);
		}
	}

	public FileTypeEnum getFileType() {
		synchronized (this) {
			FileTypeEnum type = (FileTypeEnum) getAttribute(ScopeAttribute.SEARCH_FILE_FILE_TYPE);
			if (type == null)
				return FileTypeEnum.ALL;
			return type;
		}
	}

	public void setFileName(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_FILE_NAME, v);
		}
	}

	public String getFileName() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_FILE_FILE_NAME);
		}
	}

	public void setPageSize(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_SHEET_ROWS, v);
		}
	}

	public Integer getPageSize() {
		synchronized (this) {
			Integer v = (Integer) getAttribute(ScopeAttribute.SEARCH_FILE_SHEET_ROWS);
			if (v == null)
				v = 10;
			return v;
		}
	}

	public void setCrawlDateStart(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_DATE_START, v);
		}
	}

	public Date getCrawlDateStart() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_FILE_DATE_START);
		}
	}

	public void setCrawlDateEnd(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_DATE_END, v);
		}
	}

	public Date getCrawlDateEnd() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_FILE_DATE_END);
		}
	}

	public void setDateModifiedStart(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_START, v);
		}
	}

	public Date getDateModifiedStart() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_START);
		}
	}

	public void setDateModifiedEnd(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_END, v);
		}
	}

	public Date getDateModifiedEnd() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_FILE_DATE_MODIFIED_END);
		}
	}

	public void setRepository(String v) {
		synchronized (this) {
			if (v != null)
				if (v.length() == 0)
					v = null;
			setAttribute(ScopeAttribute.SEARCH_FILE_REPOSITORY, v);
		}
	}

	public String getRepository() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_FILE_REPOSITORY);
		}
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			fileList = null;
			activePage = 0;
			totalSize = 0;
			reload();
		}
	}

	private AbstractSearchRequest getSearchRequest(FileManager fileManager,
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
			AbstractSearchRequest searchRequest = getSearchRequest(fileManager,
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

	@Command
	public void onSetToUnfetched() throws SearchLibException,
			InterruptedException {
		synchronized (this) {
			FileManager fileManager = getFileManager();
			if (fileManager == null)
				return;
			AbstractSearchRequest searchRequest = getSearchRequest(fileManager,
					SearchTemplate.fileSearch);
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setSelection(searchRequest, false, true);
			onTask(taskFileManagerAction);
		}
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		synchronized (this) {
			FileManager fileManager = getFileManager();
			if (fileManager == null)
				return;
			AbstractSearchRequest searchRequest = getSearchRequest(fileManager,
					SearchTemplate.fileExport);
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setSelection(searchRequest, true, false);
			onTask(taskFileManagerAction);
		}
	}

	@Command
	public void onOptimize() throws SearchLibException, InterruptedException {
		synchronized (this) {
			TaskFileManagerAction taskFileManagerAction = new TaskFileManagerAction();
			taskFileManagerAction.setOptimize();
			onTask(taskFileManagerAction);
		}
	}

	public String getBatchCommand() {
		return batchCommand;
	}

	public void setBatchCommand(String cmd) {
		batchCommand = cmd;
	}

	@Command
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
			if ("setToUnfetched".equalsIgnoreCase(batchCommand))
				onSetToUnfetched();
			else if ("delete".equalsIgnoreCase(batchCommand))
				onDelete();
			else if ("optimize".equalsIgnoreCase(batchCommand))
				onOptimize();
			batchCommand = null;
		}
	}

	@Override
	@NotifyChange("currentTaskLog")
	public void onTimer() {
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		FileManager fileManager = getFileManager();
		if (fileManager == null)
			return false;
		return fileManager.isCurrentTaskLogExists();
	}
}
