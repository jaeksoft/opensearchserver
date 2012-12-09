/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class UrlFilterController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2419049507881498895L;

	private class DeleteAlert extends AlertController {

		private transient UrlFilterItem urlFilterItem;

		protected DeleteAlert(UrlFilterItem urlFilterItem)
				throws InterruptedException {
			super("Please, confirm that you want to delete the URL filter: "
					+ urlFilterItem.getName(), Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.urlFilterItem = urlFilterItem;
		}

		@Override
		protected void onYes() throws SearchLibException {
			getUrlFilterList().remove(urlFilterItem);
			getClient().saveUrlFilterList();
			onCancel();
		}
	}

	private transient UrlFilterItem selectedFilter;

	private transient UrlFilterItem currentFilter;

	public UrlFilterController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFilter = null;
		currentFilter = new UrlFilterItem(null, null);
	}

	public UrlFilterList getUrlFilterList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getUrlFilterList();
	}

	/**
	 * @param selectedFilter
	 *            the selectedFilter to set
	 * @throws SearchLibException
	 */
	public void setSelectedFilter(UrlFilterItem selectedFilter)
			throws SearchLibException {
		this.selectedFilter = selectedFilter;
		selectedFilter.copyTo(currentFilter);
		reloadPage();
	}

	/**
	 * @return the selectedFilter
	 */
	public UrlFilterItem getSelectedFilter() {
		return selectedFilter;
	}

	public boolean isSelected() {
		return selectedFilter != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	/**
	 * @return the currentFilter
	 */
	public UrlFilterItem getCurrentFilter() {
		return currentFilter;
	}

	public void onSave() throws InterruptedException, SearchLibException {
		if (selectedFilter != null)
			currentFilter.copyTo(selectedFilter);
		else
			getClient().getUrlFilterList().add(currentFilter);
		getClient().saveUrlFilterList();
		onCancel();
	}

	public void onCancel() throws SearchLibException {
		currentFilter = new UrlFilterItem(null, null);
		selectedFilter = null;
		reloadPage();
	}

	public void delete(Component comp) throws SearchLibException,
			InterruptedException {
		UrlFilterItem item = (UrlFilterItem) comp.getAttribute("urlfilteritem");
		if (item == null)
			return;
		new DeleteAlert(item);
	}

}
