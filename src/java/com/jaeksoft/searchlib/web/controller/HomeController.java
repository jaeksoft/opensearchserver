/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.util.Set;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.template.TemplateList;

public class HomeController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2896471240596574094L;

	private transient String indexName;

	private transient TemplateList indexTemplate;

	private transient Set<ClientCatalogItem> catalogItems;

	public HomeController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		indexName = "";
		indexTemplate = TemplateList.EMPTY_INDEX;
		catalogItems = null;
	}

	public Set<ClientCatalogItem> getClientCatalog() throws SearchLibException {
		if (catalogItems != null)
			return catalogItems;
		catalogItems = ClientCatalog.getClientCatalog(getLoggedUser());
		return catalogItems;
	}

	public ClientCatalogItem getClientName() throws SearchLibException {
		Client client = super.getClient();
		if (client == null)
			return null;
		return new ClientCatalogItem(client.getIndexName());
	}

	public boolean isSelectedIndex() throws SearchLibException {
		return getClientName() != null;
	}

	public void setClientName(ClientCatalogItem item)
			throws SearchLibException, NamingException {
		Client client = ClientCatalog.getClient(item.getIndexName());
		if (client == null)
			return;
		setClient(client);
	}

	public String getNewIndexName() {
		return indexName;
	}

	public void setNewIndexName(String indexName) {
		this.indexName = indexName;
	}

	public TemplateList[] getTemplateList() {
		return TemplateList.values();
	}

	public TemplateList getNewIndexTemplate() {
		return indexTemplate;
	}

	public void setNewIndexTemplate(TemplateList indexTemplate) {
		this.indexTemplate = indexTemplate;
		reloadPage();
	}

	public void onNewIndex() throws SearchLibException, InterruptedException,
			IOException, NamingException {
		String msg = null;
		if (indexName == null)
			msg = "Please enter a valid name for the new index";
		else if (indexName.length() == 0)
			msg = "Please enter a valid name for the new index";
		else if (ClientCatalog.exists(getLoggedUser(), indexName))
			msg = "The name already exists";

		if (msg != null) {
			new AlertController(msg);
			return;
		}
		ClientCatalog.createIndex(getLoggedUser(), indexName,
				indexTemplate.getTemplate());
		setClient(ClientCatalog.getClient(indexName));
	}

	public void onListRefresh() {
		catalogItems = null;
		reloadPage();
	}

	private class EraseIndexAlert extends AlertController {

		private transient String indexName;

		protected EraseIndexAlert(String indexName) throws InterruptedException {
			super("Please, confirm that you want to erase the index: "
					+ indexName + ". All the datas will be erased",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.indexName = indexName;
		}

		@Override
		protected void onYes() throws SearchLibException {
			try {
				ClientCatalog.eraseIndex(getLoggedUser(), indexName);
				setClient(null);
			} catch (NamingException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			}
		}

	}

	public void eraseIndex(Component comp) throws SearchLibException,
			InterruptedException {
		if (comp == null)
			return;
		ClientCatalogItem item = (ClientCatalogItem) comp
				.getAttribute("catalogitem");
		if (item == null)
			return;
		new EraseIndexAlert(item.getIndexName());
	}

	public void computeInfos(Component comp) throws SearchLibException {
		if (comp == null)
			return;
		ClientCatalogItem item = (ClientCatalogItem) comp
				.getAttribute("catalogitem");
		if (item == null)
			return;
		item.computeInfos();
		reloadPage();
	}

}
