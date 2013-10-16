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

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.scheduler.task.TaskUrlManagerAction;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UrlController extends CommonController {

	public static enum BatchCommandEnum {

		NOTHING("Select an action"),

		EXPORT_TXT("Export URLs"),

		XML_SITEMAP("Export XML SiteMap"),

		SET_TO_UNFETCHED("Set selected URLs to Unfetched"),

		SET_TO_FETCH_FIRST("Set selected URLs to fetch first"),

		LOAD_SITEMAP("Load Sitemap(s)"),

		DELETE_URL("Delete selected URLs"),

		OPTIMIZE("Optimize URL database"),

		SYNCHRONIZE_INDEX("Synchronize the selected URLs with the index"),

		DELETE_ALL("Delete all URLs");

		private final String label;

		private BatchCommandEnum(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	public class ColView {

		private boolean date = false;

		private boolean contentType = false;

		private boolean charsetEncoding = false;

		private boolean language = false;

		private boolean returnedCode = false;

		public void setDate(boolean b) {
			this.date = b;
		}

		public boolean isDate() {
			return date;
		}

		/**
		 * @return the contentType
		 */
		public boolean isContentType() {
			return contentType;
		}

		/**
		 * @param contentType
		 *            the contentType to set
		 */
		public void setContentType(boolean contentType) {
			this.contentType = contentType;
		}

		/**
		 * @return the charsetEncoding
		 */
		public boolean isCharsetEncoding() {
			return charsetEncoding;
		}

		/**
		 * @param charsetEncoding
		 *            the charsetEncoding to set
		 */
		public void setCharsetEncoding(boolean charsetEncoding) {
			this.charsetEncoding = charsetEncoding;
		}

		/**
		 * @return the language
		 */
		public boolean isLanguage() {
			return language;
		}

		/**
		 * @param language
		 *            the language to set
		 */
		public void setLanguage(boolean language) {
			this.language = language;
		}

		/**
		 * @return the returnedCode
		 */
		public boolean isReturnedCode() {
			return returnedCode;
		}

		/**
		 * @param returnedCode
		 *            the returnedCode to set
		 */
		public void setReturnedCode(boolean returnedCode) {
			this.returnedCode = returnedCode;
		}
	}

	private transient ColView colView;

	private transient List<UrlItem> urlList;

	private transient int totalSize;

	private transient int activePage;

	private transient BatchCommandEnum batchCommand;

	public UrlController() throws SearchLibException {
		super();
		colView = new ColView();
	}

	@Override
	protected void reset() {
		urlList = null;
		totalSize = 0;
		activePage = 0;
		batchCommand = BatchCommandEnum.NOTHING;
	}

	public ColView getColView() {
		return colView;

	}

	public int getActivePage() {
		return activePage;
	}

	public void setActivePage(int page) throws SearchLibException {
		synchronized (this) {
			activePage = page;
			computeUrlList();
			reload();
		}
	}

	public int getTotalSize() {
		return totalSize;
	}

	public long getRecordNumber() throws SearchLibException {
		synchronized (this) {
			UrlManager urlManager = getUrlManager();
			if (urlManager == null)
				return 0;
			return urlManager.getSize();
		}
	}

	public void setHost(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_HOST, v);
		}
	}

	public String getHost() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_HOST);
		}
	}

	public void setBufferSize(int v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_BUFFER_SIZE, new Integer(v));
		}
	}

	public int getBufferSize() {
		synchronized (this) {
			Integer v = (Integer) getAttribute(ScopeAttribute.SEARCH_URL_BUFFER_SIZE);
			return v == null ? 10000 : v;
		}
	}

	public boolean isWithSubDomain() {
		synchronized (this) {
			Boolean b = (Boolean) getAttribute(ScopeAttribute.SEARCH_URL_SUBHOST);
			if (b != null)
				return b;
			return false;
		}
	}

	public void setWithSubDomain(boolean b) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_SUBHOST, new Boolean(b));
		}
	}

	public void setResponseCode(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_RESPONSE_CODE, v);
		}
	}

	public Integer getResponseCode() {
		synchronized (this) {
			return (Integer) getAttribute(ScopeAttribute.SEARCH_URL_RESPONSE_CODE);
		}
	}

	public void setLang(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_LANG, v);
		}
	}

	public String getLang() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_LANG);
		}
	}

	public void setLangMethod(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_LANG_METHOD, v);
		}
	}

	public String getLangMethod() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_LANG_METHOD);
		}
	}

	public void setContentBaseType(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_CONTENT_BASE_TYPE, v);
		}
	}

	public String getContentBaseType() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_CONTENT_BASE_TYPE);
		}
	}

	public void setContentTypeCharset(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_CONTENT_TYPE_CHARSET, v);
		}
	}

	public String getContentTypeCharset() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_CONTENT_TYPE_CHARSET);
		}
	}

	public void setContentEncoding(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_CONTENT_ENCODING, v);
		}
	}

	public String getContentEncoding() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_CONTENT_ENCODING);
		}
	}

	public void setMinContentLength(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_MIN_CONTENT_LENGTH, v);
		}
	}

	public Integer getMinContentLength() {
		synchronized (this) {
			return (Integer) getAttribute(ScopeAttribute.SEARCH_URL_MIN_CONTENT_LENGTH);
		}
	}

	public void setMaxContentLength(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_MAX_CONTENT_LENGTH, v);
		}
	}

	public Integer getMaxContentLength() {
		synchronized (this) {
			return (Integer) getAttribute(ScopeAttribute.SEARCH_URL_MAX_CONTENT_LENGTH);
		}
	}

	public void setFetchStatus(FetchStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_FETCH_STATUS, v);
		}
	}

	public FetchStatus getFetchStatus() {
		synchronized (this) {
			FetchStatus st = (FetchStatus) getAttribute(ScopeAttribute.SEARCH_URL_FETCH_STATUS);
			if (st == null)
				return FetchStatus.ALL;
			return st;
		}
	}

	public void setParserStatus(ParserStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_PARSER_STATUS, v);
		}
	}

	public ParserStatus getParserStatus() {
		synchronized (this) {
			ParserStatus status = (ParserStatus) getAttribute(ScopeAttribute.SEARCH_URL_PARSER_STATUS);
			if (status == null)
				return ParserStatus.ALL;
			return status;
		}
	}

	public void setIndexStatus(IndexStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_INDEX_STATUS, v);
		}
	}

	public IndexStatus getIndexStatus() {
		synchronized (this) {
			IndexStatus status = (IndexStatus) getAttribute(ScopeAttribute.SEARCH_URL_INDEX_STATUS);
			if (status == null)
				return IndexStatus.ALL;
			return status;
		}
	}

	public void setRobotsTxtStatus(RobotsTxtStatus v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_ROBOTSTXT_STATUS, v);
		}
	}

	public RobotsTxtStatus getRobotsTxtStatus() {
		synchronized (this) {
			RobotsTxtStatus status = (RobotsTxtStatus) getAttribute(ScopeAttribute.SEARCH_URL_ROBOTSTXT_STATUS);
			if (status == null)
				return RobotsTxtStatus.ALL;
			return status;
		}
	}

	public void setLike(String v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_LIKE, v);
		}
	}

	public String getLike() {
		synchronized (this) {
			return (String) getAttribute(ScopeAttribute.SEARCH_URL_LIKE);

		}
	}

	public void setPageSize(Integer v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_SHEET_ROWS, v);
		}
	}

	public Integer getPageSize() {
		synchronized (this) {
			Integer v = (Integer) getAttribute(ScopeAttribute.SEARCH_URL_SHEET_ROWS);
			if (v == null)
				v = 10;
			return v;
		}
	}

	public void setEventDateStart(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_DATE_START, v);
		}
	}

	public Date getEventDateStart() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_URL_DATE_START);
		}
	}

	public void setEventDateEnd(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_DATE_END, v);
		}
	}

	public Date getEventDateEnd() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_URL_DATE_END);
		}
	}

	public void setModifiedDateStart(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_DATE_MODIFIED_START, v);
		}
	}

	public Date getModifiedDateStart() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_URL_DATE_MODIFIED_START);
		}
	}

	public void setModifiedDateEnd(Date v) {
		synchronized (this) {
			setAttribute(ScopeAttribute.SEARCH_URL_DATE_MODIFIED_END, v);
		}
	}

	public Date getModifiedDateEnd() {
		synchronized (this) {
			return (Date) getAttribute(ScopeAttribute.SEARCH_URL_DATE_MODIFIED_END);
		}
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			activePage = 0;
			computeUrlList();
			reload();
		}
	}

	private AbstractSearchRequest getSearchRequest(
			SearchTemplate urlSearchTemplate) throws SearchLibException {
		UrlManager urlManager = getUrlManager();
		if (urlManager == null)
			return null;
		return urlManager.getSearchRequest(urlSearchTemplate, getLike(),
				getHost(), isWithSubDomain(), getLang(), getLangMethod(),
				getContentBaseType(), getContentTypeCharset(),
				getContentEncoding(), getMinContentLength(),
				getMaxContentLength(), getRobotsTxtStatus(), getFetchStatus(),
				getResponseCode(), getParserStatus(), getIndexStatus(),
				getEventDateStart(), getEventDateEnd(), getModifiedDateStart(),
				getModifiedDateEnd());
	}

	private void computeUrlList() throws SearchLibException {
		synchronized (this) {
			UrlManager urlManager = getUrlManager();
			if (urlManager == null)
				return;
			urlList = new ArrayList<UrlItem>();
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlSearch);
			totalSize = (int) urlManager.getUrlList(searchRequest,
					getPageSize() * getActivePage(), getPageSize(), urlList);
		}
	}

	public List<UrlItem> getUrlList() throws SearchLibException {
		synchronized (this) {
			return urlList;
		}
	}

	public UrlManager getUrlManager() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getUrlManager();
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

	public void onExportSiteMap() throws IOException, SearchLibException,
			TransformerConfigurationException, SAXException {
		synchronized (this) {
			UrlManager urlManager = getUrlManager();
			if (urlManager == null)
				return;
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			File file = urlManager.exportSiteMap(searchRequest);
			Filedownload.save(new FileInputStream(file),
					"text/xml; charset-UTF-8", "OSS_SiteMap.xml");
		}
	}

	public void onExportURLs() throws IOException, SearchLibException {
		synchronized (this) {
			UrlManager urlManager = getUrlManager();
			if (urlManager == null)
				return;
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			File file = urlManager.exportURLs(searchRequest);
			Filedownload.save(new FileInputStream(file),
					"text/plain; charset-UTF-8", "OSS_URLs_Export.txt");
		}
	}

	private void onTask(TaskUrlManagerAction taskUrlManagerAction)
			throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		TaskItem taskItem = new TaskItem(client, taskUrlManagerAction);
		TaskManager.executeTask(client, taskItem, null);
		client.getUrlManager().waitForTask(taskUrlManagerAction, 30);
	}

	public void onSetToUnfetched() throws SearchLibException,
			InterruptedException {
		synchronized (this) {
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlSearch);
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(searchRequest,
					FetchStatus.UN_FETCHED,
					TaskUrlManagerAction.CommandOptimize, getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public void onSetToFetchFirst() throws SearchLibException,
			InterruptedException {
		synchronized (this) {
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlSearch);
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(searchRequest,
					FetchStatus.FETCH_FIRST,
					TaskUrlManagerAction.CommandOptimize, getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public void onLoadSitemap() throws SearchLibException, InterruptedException {
		synchronized (this) {
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(null, null,
					TaskUrlManagerAction.CommandLoadSitemap, getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public void onDeleteURLs() throws SearchLibException, InterruptedException {
		synchronized (this) {
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(searchRequest, null,
					TaskUrlManagerAction.CommandDeleteSelection,
					getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public void onSynchronizedIndex() throws SearchLibException,
			InterruptedException {
		synchronized (this) {
			AbstractSearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(searchRequest, null,
					TaskUrlManagerAction.CommandSynchronize, getBufferSize());
			onTask(taskUrlManagerAction);
		}

	}

	public void onOptimize() throws SearchLibException, InterruptedException {
		synchronized (this) {
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(null, null,
					TaskUrlManagerAction.CommandOptimize, getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public void onDeleteAll() throws SearchLibException, InterruptedException {
		synchronized (this) {
			TaskUrlManagerAction taskUrlManagerAction = new TaskUrlManagerAction();
			taskUrlManagerAction.setManual(null, null,
					TaskUrlManagerAction.CommandDeleteAll, getBufferSize());
			onTask(taskUrlManagerAction);
		}
	}

	public BatchCommandEnum getBatchCommand() {
		return batchCommand;
	}

	public void setBatchCommand(BatchCommandEnum batchCommand) {
		this.batchCommand = batchCommand;
	}

	public BatchCommandEnum[] getBatchCommandEnum() {
		return BatchCommandEnum.values();
	}

	@Command
	public void onGo() throws SearchLibException, IOException,
			TransformerConfigurationException, SAXException,
			InterruptedException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			if (client.getWebCrawlMaster().isRunning()) {
				new AlertController("Please stop the Web crawler first.");
				return;
			}
			if (batchCommand == null)
				return;
			switch (batchCommand) {
			case NOTHING:
				break;
			case EXPORT_TXT:
				onExportURLs();
				break;
			case XML_SITEMAP:
				onExportSiteMap();
				break;
			case SET_TO_UNFETCHED:
				onSetToUnfetched();
				break;
			case SET_TO_FETCH_FIRST:
				onSetToFetchFirst();
				break;
			case DELETE_URL:
				onDeleteURLs();
				break;
			case OPTIMIZE:
				onOptimize();
				break;
			case DELETE_ALL:
				onDeleteAll();
				break;
			case LOAD_SITEMAP:
				onLoadSitemap();
				break;
			case SYNCHRONIZE_INDEX:
				onSynchronizedIndex();
				break;
			}
			batchCommand = BatchCommandEnum.NOTHING;
			reload();
		}
	}

	@Command
	@NotifyChange("urlManager")
	public void onRefreshCurrentTaskLog() {
	}

	@Command
	@NotifyChange("*")
	public void onChangeColView() {
	}
}
