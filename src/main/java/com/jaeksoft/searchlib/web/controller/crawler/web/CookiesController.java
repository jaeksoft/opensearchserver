/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.database.CookieManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

@AfterCompose(superclass = true)
public class CookiesController extends CrawlerController {

	private transient List<CookieItem> cookieList;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	private transient CookieItem selectedCookie;

	private transient CookieItem currentCookie;

	private class DeleteAlert extends AlertController {

		private transient CookieItem deleteCookie;

		protected DeleteAlert(CookieItem deleteCookie)
				throws InterruptedException {
			super("Please, confirm that you want to delete the cookie: "
					+ deleteCookie.getPattern() + " " + deleteCookie.getName(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.deleteCookie = deleteCookie;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			CookieManager cookieManager = client.getWebCookieManager();
			cookieManager.delItem(deleteCookie);
			onCancel();
		}
	}

	public CookiesController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		cookieList = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selectedCookie = null;
		currentCookie = new CookieItem();
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

	public void setActivePage(int page) throws SearchLibException {
		this.activePage = page;
		this.cookieList = null;
		reload();
	}

	public int getTotalSize() {
		return totalSize;
	}

	public List<CookieItem> getCookieList() {
		synchronized (this) {
			if (cookieList != null)
				return cookieList;
			try {
				Client client = getClient();
				if (client == null)
					return null;
				CookieManager cookieManager = client.getWebCookieManager();
				cookieList = new ArrayList<CookieItem>(0);
				totalSize = cookieManager.getItems(null, getActivePage()
						* getPageSize(), getPageSize(), cookieList);
				return cookieList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			cookieList = null;
			activePage = 0;
			totalSize = 0;
			reload();
		}
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedCookie == null ? "Create a new cookie"
				: "Edit the selected cookie";
	}

	/**
	 * 
	 * @return the current cookie item
	 */
	public CookieItem getCurrentCookie() {
		return currentCookie;
	}

	/**
	 * 
	 * @return the selected cookie item
	 */
	public CookieItem getSelectedCookie() {
		return selectedCookie;
	}

	/**
	 * Set the selected credential
	 * 
	 * @param credential
	 * @throws SearchLibException
	 */
	public void setSelectedCookie(CookieItem cookie) throws SearchLibException {
		if (cookie == null)
			return;
		selectedCookie = cookie;
		selectedCookie.copyTo(currentCookie);
		reload();
	}

	public boolean isSelected() {
		return selectedCookie != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			cookieList = null;
			super.reload();
		}
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
		reload();
	}

	@Command
	@NotifyChange("currentCredential")
	public void reloadCurrentCredential() {
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		if (selectedCookie == null)
			return;
		if (!isWebCrawlerEditPatternsRights())
			throw new SearchLibException("Not allowed");
		new DeleteAlert(selectedCookie);
		onCancel();
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException,
			MalformedURLException, URISyntaxException {
		Client client = getClient();
		if (client == null)
			return;
		CookieManager cookieManager = client.getWebCookieManager();
		if (selectedCookie == null) {
			cookieManager.addItem(currentCookie);
		} else {
			currentCookie.copyTo(selectedCookie);
			cookieManager.updateItem(selectedCookie);
		}
		onCancel();
	}

}