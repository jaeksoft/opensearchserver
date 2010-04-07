/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.util.Set;

import javax.naming.NamingException;

import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.template.TemplateList;

public class HomeController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2896471240596574094L;

	private String indexName;

	private TemplateList indexTemplate;

	public HomeController() throws SearchLibException, NamingException {
		super();
		indexName = "";
		indexTemplate = TemplateList.EMPTY_INDEX;
	}

	public Set<String> getClientCatalog() throws SearchLibException {
		return ClientCatalog.getClientCatalog(getLoggedUser());
	}

	public String getClientName() throws SearchLibException {
		Client client = super.getClient();
		if (client == null)
			return null;
		return client.getIndexDirectory().getName();
	}

	public boolean isSelectedIndex() throws SearchLibException {
		return getClientName() != null;
	}

	public void setClientName(String name) throws SearchLibException,
			NamingException {
		Client client = ClientCatalog.getClient(name);
		if (client == null)
			return;
		setClient(client);
		resetDesktop();
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
		ClientCatalog.createIndex(getLoggedUser(), indexName, indexTemplate
				.getTemplate());
		setClient(ClientCatalog.getClient(indexName));
		reloadDesktop();
	}

	public void onListRefresh() {
		reloadPage();
	}

	private class EraseIndexAlert extends AlertController {

		private String indexName;

		protected EraseIndexAlert(String indexName) throws InterruptedException {
			super("Please, confirm that you want to erase the index: "
					+ indexName + ". All the datas will be erased",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.indexName = indexName;
		}

		@Override
		protected void onYes() throws SearchLibException {
			try {
				setClient(null);
				ClientCatalog.eraseIndex(getLoggedUser(), indexName);
				reloadDesktop();
			} catch (NamingException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			}
		}

	}

	public void onEraseIndex() throws SearchLibException, InterruptedException {
		String indexName = getClientName();
		if (indexName == null)
			return;
		new EraseIndexAlert(indexName);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
