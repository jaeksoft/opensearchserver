/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class IndexController extends CommonController {

	private String indexChoice;

	public IndexController() throws SearchLibException {
		super();
	}

	public List<IndexAbstract> getIndices() throws SearchLibException,
			NamingException {
		List<IndexAbstract> list = new ArrayList<IndexAbstract>();
		Client client = getClient();
		if (client == null)
			return list;
		IndexAbstract index = client.getIndexAbstract();
		list.add(index);
		return list;
	}

	@Override
	protected void reset() {
	}

	public List<String> getIndexList() throws SearchLibException {
		return getClient().getIndex().getIndexConfig().getIndexList();
	}

	public String getIndexChoice() {
		return indexChoice;
	}

	public void setIndexChoice(String indexChoice) {
		this.indexChoice = indexChoice;
	}

	@Command
	@NotifyChange({ "indexList", "indices" })
	public void onAddIndex() throws SearchLibException {
		if (!ClientCatalog.exists(getLoggedUser(), indexChoice))
			throw new SearchLibException("Index not found");
		Client client = getClient();
		client.getIndex().getIndexConfig().addIndex(indexChoice);
		client.saveConfig();
		client.reload();
	}

	@Command
	@NotifyChange({ "indexList", "indices" })
	public void onRemoveIndex(@BindingParam("indexName") String indexName)
			throws SearchLibException {
		Client client = getClient();
		client.getIndex().getIndexConfig().removeIndex(indexName);
		client.saveConfig();
		client.reload();
	}

	public boolean isMulti() throws SearchLibException {
		return getClient().getIndex().getIndexConfig().isMulti();
	}

}
