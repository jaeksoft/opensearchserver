/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;

import javax.naming.NamingException;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.template.TemplateList;

public class HomeController extends CommonController implements
		ListitemRenderer {

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

	public File[] getClientCatalog() throws SearchLibException {
		return ClientCatalog.getClientCatalog();
	}

	public File getClientFile() throws SearchLibException {
		Client client = super.getClient();
		if (client == null)
			return null;
		return client.getIndexDirectory();
	}

	public void setClientFile(File file) throws SearchLibException,
			NamingException {
		Client client = ClientCatalog.getClient(file.getName());
		if (client == null)
			return;
		setClient(client);
		reloadDesktop();
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
			IOException {
		String msg = null;
		if (indexName == null)
			msg = "Please enter a valid name for the new index";
		else if (indexName.length() == 0)
			msg = "Please enter a valid name for the new index";
		else if (ClientCatalog.exists(indexName))
			msg = "The name already exists";

		if (msg != null) {
			Messagebox.show(msg, "Jaeksoft OpenSearchServer", Messagebox.OK,
					org.zkoss.zul.Messagebox.EXCLAMATION);
			return;
		}
		ClientCatalog.createIndex(indexName, indexTemplate.getTemplate());
		reloadPage();
	}

	public void render(Listitem item, Object data) throws Exception {
		File indexDirectory = (File) data;
		new Listcell(indexDirectory.getName()).setParent(item);
	}
}
