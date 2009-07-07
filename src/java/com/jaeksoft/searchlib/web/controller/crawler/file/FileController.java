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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.web.database.FileItem;
import com.jaeksoft.searchlib.crawler.web.database.FileManager;
import com.jaeksoft.searchlib.crawler.web.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.web.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class FileController extends CommonController implements AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2429849843906661914L;

	private List<FileItem> fileList;

	private int totalSize;

	private int activePage;

	public FileController() throws SearchLibException {
		super();
		fileList = null;
		totalSize = 0;
		activePage = 0;
	}

	public void afterCompose() {
		getFellow("paging").addEventListener("onPaging", new EventListener() {
			public void onEvent(Event event) {
				onPaging((PagingEvent) event);
			}
		});
	}

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setHost(String v) {
		synchronized (this) {
			setAttribute("searchUrlHost", v, SESSION_SCOPE);
		}
	}

	public String getHost() {
		synchronized (this) {
			return (String) getAttribute("searchUrlHost", SESSION_SCOPE);
		}
	}

	public void setResponseCode(Integer v) {
		synchronized (this) {
			setAttribute("searchUrlResponseCode", v, SESSION_SCOPE);
		}
	}

	public Integer getResponseCode() {
		synchronized (this) {
			return (Integer) getAttribute("searchUrlResponseCode",
					SESSION_SCOPE);
		}
	}

	public void setLang(String v) {
		synchronized (this) {
			setAttribute("searchUrlLang", v, SESSION_SCOPE);
		}
	}

	public String getLang() {
		synchronized (this) {
			return (String) getAttribute("searchUrlLang", SESSION_SCOPE);
		}
	}

	public void setLangMethod(String v) {
		synchronized (this) {
			setAttribute("searchUrlLangMethod", v, SESSION_SCOPE);
		}
	}

	public String getLangMethod() {
		synchronized (this) {
			return (String) getAttribute("searchUrlMethod", SESSION_SCOPE);
		}
	}

	public void setContentBaseType(String v) {
		synchronized (this) {
			setAttribute("searchUrlContentBaseType", v, SESSION_SCOPE);
		}
	}

	public String getContentBaseType() {
		synchronized (this) {
			return (String) getAttribute("searchUrlContentBaseType",
					SESSION_SCOPE);
		}
	}

	public void setContentTypeCharset(String v) {
		synchronized (this) {
			setAttribute("searchUrlContentCharset", v, SESSION_SCOPE);
		}
	}

	public String getContentTypeCharset() {
		synchronized (this) {
			return (String) getAttribute("searchUrlContentCharset",
					SESSION_SCOPE);
		}
	}

	public void setContentEncoding(String v) {
		synchronized (this) {
			setAttribute("searchUrlContentEncoding", v, SESSION_SCOPE);
		}
	}

	public String getContentEncoding() {
		synchronized (this) {
			return (String) getAttribute("searchUrlContentEncoding",
					SESSION_SCOPE);
		}
	}

	public void setMinContentLength(Integer v) {
		synchronized (this) {
			setAttribute("searchUrlMinContentLength", v, SESSION_SCOPE);
		}
	}

	public Integer getMinContentLength() {
		synchronized (this) {
			return (Integer) getAttribute("searchUrlMinContentLength",
					SESSION_SCOPE);
		}
	}

	public void setMaxContentLength(Integer v) {
		synchronized (this) {
			setAttribute("searchUrlMaxContentLength", v, SESSION_SCOPE);
		}
	}

	public Integer getMaxContentLength() {
		synchronized (this) {
			return (Integer) getAttribute("searchUrlMaxContentLength",
					SESSION_SCOPE);
		}
	}

	public void setFetchStatus(FetchStatus v) {
		synchronized (this) {
			setAttribute("searchUrlFetchStatus", v, SESSION_SCOPE);
		}
	}

	public FetchStatus getFetchStatus() {
		synchronized (this) {
			FetchStatus st = (FetchStatus) getAttribute("searchUrlFetchStatus",
					SESSION_SCOPE);
			if (st == null)
				return FetchStatus.ALL;
			return st;
		}
	}

	public void setParserStatus(ParserStatus v) {
		synchronized (this) {
			setAttribute("searchUrlParserStatus", v, SESSION_SCOPE);
		}
	}

	public ParserStatus getParserStatus() {
		synchronized (this) {
			ParserStatus status = (ParserStatus) getAttribute(
					"searchUrlParserStatus", SESSION_SCOPE);
			if (status == null)
				return ParserStatus.ALL;
			return status;
		}
	}

	public void setIndexStatus(IndexStatus v) {
		synchronized (this) {
			setAttribute("searchUrlIndexStatus", v, SESSION_SCOPE);
		}
	}

	public IndexStatus getIndexStatus() {
		synchronized (this) {
			IndexStatus status = (IndexStatus) getAttribute(
					"searchUrlIndexStatus", SESSION_SCOPE);
			if (status == null)
				return IndexStatus.ALL;
			return status;
		}
	}

	public void setRobotsTxtStatus(RobotsTxtStatus v) {
		synchronized (this) {
			setAttribute("searchUrlRobotsTxtStatus", v, SESSION_SCOPE);
		}
	}

	public RobotsTxtStatus getRobotsTxtStatus() {
		synchronized (this) {
			RobotsTxtStatus status = (RobotsTxtStatus) getAttribute(
					"searchUrlRobotsTxtStatus", SESSION_SCOPE);
			if (status == null)
				return RobotsTxtStatus.ALL;
			return status;
		}
	}

	public void setLike(String v) {
		synchronized (this) {
			setAttribute("searchUrlLike", v, SESSION_SCOPE);
		}
	}

	public String getLike() {
		synchronized (this) {
			return (String) getAttribute("searchUrlLike", SESSION_SCOPE);
		}
	}

	public void setPageSize(Integer v) {
		synchronized (this) {
			setAttribute("searchUrlSheetRows", v, SESSION_SCOPE);
		}
	}

	public Integer getPageSize() {
		synchronized (this) {
			Integer v = (Integer) getAttribute("searchUrlSheetRows",
					SESSION_SCOPE);
			if (v == null)
				v = 10;
			return v;
		}
	}

	public void setDateStart(Date v) {
		synchronized (this) {
			setAttribute("searchUrlDateStart", v, SESSION_SCOPE);
		}
	}

	public Date getDateStart() {
		synchronized (this) {
			return (Date) getAttribute("searchUrlDateStart", SESSION_SCOPE);
		}
	}

	public void setDateEnd(Date v) {
		synchronized (this) {
			setAttribute("searchUrlDateEnd", v, SESSION_SCOPE);
		}
	}

	public Date getDateEnd() {
		synchronized (this) {
			return (Date) getAttribute("searchUrlDateEnd", SESSION_SCOPE);
		}
	}

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			fileList = null;
			activePage = pagingEvent.getActivePage();
			reloadPage();
		}
	}

	public void onSearch() {
		synchronized (this) {
			fileList = null;
			activePage = 0;
			totalSize = 0;
			reloadPage();
		}
	}

	public void onReload() {
		synchronized (this) {
			reloadPage();
		}
	}

	public List<FileItem> getFileList() throws SearchLibException {
		synchronized (this) {
			if (fileList != null)
				return fileList;

			fileList = new ArrayList<FileItem>();
			FileManager fileManager = getClient().getFileManager();
			SearchRequest searchRequest = fileManager.fileQuery(getLike(),
					getLang(), getLangMethod(), getContentBaseType(),
					getContentTypeCharset(), getContentEncoding(),
					getMinContentLength(), getMaxContentLength(),
					getFetchStatus(), getResponseCode(), getParserStatus(),
					getIndexStatus(), getDateStart(), getDateEnd());

			totalSize = (int) fileManager.getFiles(searchRequest, null, false,
					getPageSize() * getActivePage(), getPageSize(), fileList);
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

}
