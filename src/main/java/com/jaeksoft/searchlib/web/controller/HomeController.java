/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexType;
import com.jaeksoft.searchlib.template.TemplateList;

@AfterCompose(superclass = true)
public class HomeController extends CommonController {

	private String indexName;

	private TemplateList indexTemplate;

	private IndexType indexType;

	private Set<ClientCatalogItem> catalogItems;

	private ClientCatalogItem selectedClientCatalogItem;

	public HomeController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		indexName = null;
		indexTemplate = TemplateList.EMPTY_INDEX;
		indexType = IndexType.LUCENE;
		catalogItems = null;
	}

	public Set<ClientCatalogItem> getClientCatalog() throws SearchLibException {
		if (catalogItems != null)
			return catalogItems;
		catalogItems = ClientCatalog.getClientCatalog(getLoggedUser());
		return catalogItems;
	}

	public ClientCatalogItem getClientName() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		if (selectedClientCatalogItem == null)
			return null;
		if (!selectedClientCatalogItem.getIndexName().equals(
				client.getIndexName()))
			return null;
		return selectedClientCatalogItem;
	}

	public boolean isSelectedIndex() throws SearchLibException {
		return getClientName() != null;
	}

	public void setClientName(ClientCatalogItem item)
			throws SearchLibException, NamingException {
		if (item == null)
			return;
		Client client = ClientCatalog.getClient(item.getIndexName());
		if (client == null)
			return;
		setClient(client);
		selectedClientCatalogItem = item;
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

	public void setNewIndexTemplate(TemplateList indexTemplate)
			throws SearchLibException {
		this.indexTemplate = indexTemplate;
		reload();
	}

	@Command
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
				indexTemplate.getTemplate(), indexType, null);
		setClient(ClientCatalog.getClient(indexName));
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
		@NotifyChange("#indexList")
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

	@Command
	public void eraseIndex(
			@BindingParam("catalogitem") ClientCatalogItem catalogItem)
			throws SearchLibException, InterruptedException {
		if (catalogItem == null)
			return;
		new EraseIndexAlert(catalogItem.getIndexName());
	}

	@Command
	@NotifyChange("clientCatalog")
	public void computeInfos(
			@BindingParam("catalogitem") ClientCatalogItem catalogItem)
			throws SearchLibException {
		if (catalogItem == null)
			return;
		catalogItem.computeInfos();
	}

	/**
	 * @return the indexType
	 */
	public IndexType getNewIndexType() {
		return indexType;
	}

	/**
	 * @param indexType
	 *            the indexType to set
	 */
	public void setNewIndexType(IndexType indexType) {
		this.indexType = indexType;
	}

	public IndexType[] getIndexTypeList() {
		return IndexType.values();
	}
}
