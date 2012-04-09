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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.event.PagingEvent;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UrlController extends CommonController implements AfterCompose {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2429849843906661914L;

	private transient List<UrlItem> urlList;

	private transient int totalSize;

	private transient int activePage;

	public UrlController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		urlList = null;
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

	public int getActivePage() {
		return activePage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public long getRecordNumber() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return 0;
			return client.getUrlManager().getSize();
		}

	}

	public void setHost(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_HOST.set(this, v);
		}
	}

	public String getHost() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_HOST.get(this);
		}
	}

	public boolean isWithSubDomain() {
		synchronized (this) {
			Boolean b = (Boolean) ScopeAttribute.SEARCH_URL_SUBHOST.get(this);
			if (b != null)
				return b;
			return false;
		}
	}

	public void setWithSubDomain(boolean b) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_SUBHOST.set(this, new Boolean(b));
		}
	}

	public void setResponseCode(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_RESPONSE_CODE.set(this, v);
		}
	}

	public Integer getResponseCode() {
		synchronized (this) {
			return (Integer) ScopeAttribute.SEARCH_URL_RESPONSE_CODE.get(this);
		}
	}

	public void setLang(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_LANG.set(this, v);
		}
	}

	public String getLang() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_LANG.get(this);
		}
	}

	public void setLangMethod(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_LANG_METHOD.set(this, v);
		}
	}

	public String getLangMethod() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_LANG_METHOD.get(this);
		}
	}

	public void setContentBaseType(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_CONTENT_BASE_TYPE.set(this, v);
		}
	}

	public String getContentBaseType() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_CONTENT_BASE_TYPE
					.get(this);
		}
	}

	public void setContentTypeCharset(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_CONTENT_TYPE_CHARSET.set(this, v);
		}
	}

	public String getContentTypeCharset() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_CONTENT_TYPE_CHARSET
					.get(this);
		}
	}

	public void setContentEncoding(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_CONTENT_ENCODING.set(this, v);
		}
	}

	public String getContentEncoding() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_CONTENT_ENCODING
					.get(this);
		}
	}

	public void setMinContentLength(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_MIN_CONTENT_LENGTH.set(this, v);
		}
	}

	public Integer getMinContentLength() {
		synchronized (this) {
			return (Integer) ScopeAttribute.SEARCH_URL_MIN_CONTENT_LENGTH
					.get(this);
		}
	}

	public void setMaxContentLength(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_MAX_CONTENT_LENGTH.set(this, v);
		}
	}

	public Integer getMaxContentLength() {
		synchronized (this) {
			return (Integer) ScopeAttribute.SEARCH_URL_MAX_CONTENT_LENGTH
					.get(this);
		}
	}

	public void setFetchStatus(FetchStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_FETCH_STATUS.set(this, v);
		}
	}

	public FetchStatus getFetchStatus() {
		synchronized (this) {
			FetchStatus st = (FetchStatus) ScopeAttribute.SEARCH_URL_FETCH_STATUS
					.get(this);
			if (st == null)
				return FetchStatus.ALL;
			return st;
		}
	}

	public void setParserStatus(ParserStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_PARSER_STATUS.set(this, v);
		}
	}

	public ParserStatus getParserStatus() {
		synchronized (this) {
			ParserStatus status = (ParserStatus) ScopeAttribute.SEARCH_URL_PARSER_STATUS
					.get(this);
			if (status == null)
				return ParserStatus.ALL;
			return status;
		}
	}

	public void setIndexStatus(IndexStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_INDEX_STATUS.set(this, v);
		}
	}

	public IndexStatus getIndexStatus() {
		synchronized (this) {
			IndexStatus status = (IndexStatus) ScopeAttribute.SEARCH_URL_INDEX_STATUS
					.get(this);
			if (status == null)
				return IndexStatus.ALL;
			return status;
		}
	}

	public void setRobotsTxtStatus(RobotsTxtStatus v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_ROBOTSTXT_STATUS.set(this, v);
		}
	}

	public RobotsTxtStatus getRobotsTxtStatus() {
		synchronized (this) {
			RobotsTxtStatus status = (RobotsTxtStatus) ScopeAttribute.SEARCH_URL_ROBOTSTXT_STATUS
					.get(this);
			if (status == null)
				return RobotsTxtStatus.ALL;
			return status;
		}
	}

	public void setLike(String v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_LIKE.set(this, v);
		}
	}

	public String getLike() {
		synchronized (this) {
			return (String) ScopeAttribute.SEARCH_URL_LIKE.get(this);

		}
	}

	public void setPageSize(Integer v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_SHEET_ROWS.set(this, v);
		}
	}

	public Integer getPageSize() {
		synchronized (this) {
			Integer v = (Integer) ScopeAttribute.SEARCH_URL_SHEET_ROWS
					.get(this);
			if (v == null)
				v = 10;
			return v;
		}
	}

	public void setEventDateStart(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_DATE_START.set(this, v);
		}
	}

	public Date getEventDateStart() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_URL_DATE_START.get(this);
		}
	}

	public void setEventDateEnd(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_DATE_END.set(this, v);
		}
	}

	public Date getEventDateEnd() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_URL_DATE_END.get(this);
		}
	}

	public void setModifiedDateStart(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_DATE_MODIFIED_START.set(this, v);
		}
	}

	public Date getModifiedDateStart() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_URL_DATE_MODIFIED_START
					.get(this);
		}
	}

	public void setModifiedDateEnd(Date v) {
		synchronized (this) {
			ScopeAttribute.SEARCH_URL_DATE_MODIFIED_END.set(this, v);
		}
	}

	public Date getModifiedDateEnd() {
		synchronized (this) {
			return (Date) ScopeAttribute.SEARCH_URL_DATE_MODIFIED_END.get(this);
		}
	}

	public void onPaging(PagingEvent pagingEvent) {
		synchronized (this) {
			activePage = pagingEvent.getActivePage();
			try {
				computeUrlList();
				reloadPage();
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void onSearch() throws SearchLibException {
		synchronized (this) {
			activePage = 0;
			computeUrlList();
			reloadPage();
		}
	}

	private SearchRequest getSearchRequest(SearchTemplate urlSearchTemplate)
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getUrlManager().getSearchRequest(urlSearchTemplate,
				getLike(), getHost(), isWithSubDomain(), getLang(),
				getLangMethod(), getContentBaseType(), getContentTypeCharset(),
				getContentEncoding(), getMinContentLength(),
				getMaxContentLength(), getRobotsTxtStatus(), getFetchStatus(),
				getResponseCode(), getParserStatus(), getIndexStatus(),
				getEventDateStart(), getEventDateEnd(), getModifiedDateStart(),
				getModifiedDateEnd());
	}

	private void computeUrlList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			urlList = new ArrayList<UrlItem>();
			SearchRequest searchRequest = getSearchRequest(SearchTemplate.urlSearch);
			totalSize = (int) client.getUrlManager().getUrlList(searchRequest,
					getPageSize() * getActivePage(), getPageSize(), urlList);
		}
	}

	public List<UrlItem> getUrlList() throws SearchLibException {
		synchronized (this) {
			return urlList;
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
			Client client = getClient();
			if (client == null)
				return;
			SearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			File file = client.getUrlManager().exportSiteMap(searchRequest);
			Filedownload.save(new FileInputStream(file),
					"text/xml; charset-UTF-8", "OSS_SiteMap.xml");
		}
	}

	public void onExportURLs() throws IOException, SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			SearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			File file = client.getUrlManager().exportURLs(searchRequest);
			Filedownload.save(new FileInputStream(file),
					"text/plain; charset-UTF-8", "OSS_URLs_Export.txt");
		}
	}

	public void onSetToUnfetched() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			UrlManager urlManager = client.getUrlManager();
			SearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			urlManager.updateFetchStatus(searchRequest, FetchStatus.UN_FETCHED);
			urlManager.reload(true);
			onSearch();
		}
	}

	public void onDeleteURLs() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			UrlManager urlManager = client.getUrlManager();
			SearchRequest searchRequest = getSearchRequest(SearchTemplate.urlExport);
			urlManager.deleteUrls(searchRequest);
			urlManager.reload(true);
			onSearch();
		}
	}

	public void onOptimize() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return;
			client.getUrlManager().reload(true);
		}
	}

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
			Listbox actionListbox = (Listbox) getFellow("actionListbox");
			String action = actionListbox.getSelectedItem().getValue()
					.toString();
			if ("exportTxt".equalsIgnoreCase(action))
				onExportURLs();
			else if ("xmlSitemap".equalsIgnoreCase(action))
				onExportSiteMap();
			else if ("setToUnfetched".equalsIgnoreCase(action))
				onSetToUnfetched();
			else if ("deleteUrls".equalsIgnoreCase(action))
				onDeleteURLs();
			else if ("optimize".equalsIgnoreCase(action))
				onOptimize();
		}
	}
}
