/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class UrlFilterController extends CrawlerController {

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

	private transient String hostname;

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
		reload();
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

	@Command
	@NotifyChange("currentFilter")
	public void addHostname() {
		if (currentFilter == null)
			return;
		currentFilter.addHostname(hostname);
	}

	@Command
	@NotifyChange("currentFilter")
	public void removeHostname(@BindingParam("hostname") String hn) {
		if (currentFilter == null)
			return;
		currentFilter.removeHostname(hn);
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException {
		if (selectedFilter != null)
			currentFilter.copyTo(selectedFilter);
		else
			getClient().getUrlFilterList().add(currentFilter);
		getClient().saveUrlFilterList();
		onCancel();
	}

	@Command
	public void onCancel() throws SearchLibException {
		currentFilter = new UrlFilterItem(null, null);
		selectedFilter = null;
		reload();
	}

	@Command
	public void delete(@BindingParam("urlfilteritem") UrlFilterItem item)
			throws SearchLibException, InterruptedException {
		new DeleteAlert(item);
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

}
