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
import com.jaeksoft.searchlib.crawler.web.database.AbstractPatternNameValueItem;
import com.jaeksoft.searchlib.crawler.web.database.AbstractPatternNameValueManager;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

@AfterCompose(superclass = true)
public abstract class AbstractNamedValueController<T extends AbstractPatternNameValueItem>
		extends CrawlerController {

	private transient List<T> itemList;

	private transient int pageSize;

	private transient int totalSize;

	private transient int activePage;

	private transient T selectedItem;

	private transient T currentItem;

	private class DeleteAlert extends AlertController {

		private transient T deleteItem;

		protected DeleteAlert(T deleteItem) throws InterruptedException {
			super("Please, confirm that you want to delete the item: "
					+ deleteItem.getPattern() + " " + deleteItem.getName(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.deleteItem = deleteItem;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			getManager(client).delItem(deleteItem);
			onCancel();
		}
	}

	protected abstract AbstractPatternNameValueManager<T> getManager(
			Client client) throws SearchLibException;

	public AbstractNamedValueController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		itemList = null;
		pageSize = 10;
		totalSize = 0;
		activePage = 0;
		selectedItem = null;
		currentItem = newItem();
	}

	protected abstract T newItem();

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
		this.itemList = null;
		reload();
	}

	public int getTotalSize() {
		return totalSize;
	}

	public List<T> getItemList() {
		synchronized (this) {
			if (itemList != null)
				return itemList;
			try {
				Client client = getClient();
				if (client == null)
					return null;
				AbstractPatternNameValueManager<T> manager = getManager(client);
				itemList = new ArrayList<T>(0);
				totalSize = manager.getItems(null, getActivePage()
						* getPageSize(), getPageSize(), itemList);
				return itemList;
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Command
	public void onSearch() throws SearchLibException {
		synchronized (this) {
			itemList = null;
			activePage = 0;
			totalSize = 0;
			reload();
		}
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedItem == null ? "Create a new item"
				: "Edit the selected item";
	}

	/**
	 * 
	 * @return the current item
	 */
	public T getCurrentItem() {
		return currentItem;
	}

	/**
	 * 
	 * @return the selected cookie item
	 */
	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Set the selected item
	 * 
	 * @param item
	 * @throws SearchLibException
	 */
	public void setSelectedItem(T item) throws SearchLibException {
		if (item == null)
			return;
		selectedItem = item;
		selectedItem.copyTo(currentItem);
		reload();
	}

	public boolean isSelected() {
		return selectedItem != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			itemList = null;
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
		if (selectedItem == null)
			return;
		if (!isWebCrawlerEditPatternsRights())
			throw new SearchLibException("Not allowed");
		new DeleteAlert(selectedItem);
		onCancel();
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException,
			MalformedURLException, URISyntaxException {
		Client client = getClient();
		if (client == null)
			return;
		AbstractPatternNameValueManager<T> manager = getManager(client);
		if (selectedItem == null) {
			manager.addItem(currentItem);
		} else {
			currentItem.copyTo(selectedItem);
			manager.updateItem(selectedItem);
		}
		onCancel();
	}

	@Command
	public void onCheck() throws InterruptedException {
		if (currentItem == null)
			return;
		new AlertController(currentItem.check());
	}

}