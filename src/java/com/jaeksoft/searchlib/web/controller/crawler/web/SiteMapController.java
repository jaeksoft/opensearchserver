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
 *  
 *  Contributor: Richard Sinelle
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.database.SiteMapList;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class SiteMapController extends CrawlerController {

	private static final long serialVersionUID = 2419049507881498895L;

	private class DeleteAlert extends AlertController {

		private transient SiteMapItem siteMapItem;

		protected DeleteAlert(SiteMapItem siteMapItem)
				throws InterruptedException {
			super("Please, confirm that you want to delete the Site Map URL: "
					+ siteMapItem.getUri(), Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.siteMapItem = siteMapItem;
		}

		@Override
		protected void onYes() throws SearchLibException {
			getSiteMapList().remove(siteMapItem);
			getClient().saveSiteMapList();
			onCancel();
		}
	}

	private transient SiteMapItem selectedSiteMap;

	private transient SiteMapItem currentSiteMap;

	public SiteMapController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSiteMap = null;
		currentSiteMap = new SiteMapItem("");
	}

	public SiteMapList getSiteMapList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSiteMapList();
	}

	/**
	 * @param selectedFilter
	 *            the selectedFilter to set
	 * @throws SearchLibException
	 */
	public void setSelectedSiteMap(SiteMapItem selectedSiteMap)
			throws SearchLibException {
		this.selectedSiteMap = selectedSiteMap;
		selectedSiteMap.copyTo(currentSiteMap);
		reloadPage();
	}

	/**
	 * @return the selectedFilter
	 */
	public SiteMapItem getSelectedSiteMap() {
		return selectedSiteMap;
	}

	public boolean isSelected() {
		return selectedSiteMap != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	/**
	 * @return the currentFilter
	 */
	public SiteMapItem getCurrentSiteMap() {
		return currentSiteMap;
	}

	public void onSave() throws InterruptedException, SearchLibException {
		if (selectedSiteMap != null)
			currentSiteMap.copyTo(selectedSiteMap);
		else
			getClient().getSiteMapList().add(currentSiteMap);
		getClient().saveSiteMapList();
		onCancel();
	}

	public void onCancel() throws SearchLibException {
		currentSiteMap = new SiteMapItem("");
		selectedSiteMap = null;
		reloadPage();
	}

	public void delete(Component comp) throws SearchLibException,
			InterruptedException {
		SiteMapItem item = (SiteMapItem) comp.getAttribute("siteMapItem");
		if (item == null)
			return;
		new DeleteAlert(item);
	}

}
