/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchRequest;

public class JoinController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7116205579213473372L;

	private JoinItem currentItem;

	private JoinItem selectedItem;

	public JoinController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedItem = null;
		currentItem = new JoinItem();
	}

	public String[] getFilterTypeList() {
		return FilterAbstract.FILTER_TYPES;
	}

	private void reloadListbox() {
		Listbox listbox = (Listbox) getFellow("joinListbox");
		listbox.invalidate();
		reloadComponent(listbox);

	}

	public void setCurrentIndexName(String indexName) {
		currentItem.setIndexName(indexName);
		reloadListbox();
	}

	public String getCurrentIndexName() {
		return currentItem.getIndexName();
	}

	public JoinItem getCurrent() {
		return currentItem;
	}

	public JoinItem getSelected() {
		return selectedItem;
	}

	public void setSelected(JoinItem item) {
		this.selectedItem = item;
		this.currentItem = new JoinItem(item);
		reloadListbox();
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadListbox();
	}

	public boolean isSelected() {
		return selectedItem != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public void onSave() throws SearchLibException {
		if (selectedItem != null)
			currentItem.copyTo(selectedItem);
		else
			((SearchRequest) getRequest()).getJoinList().add(currentItem);
		onCancel();
	}

	public List<String> getIndexList() throws SearchLibException {
		List<String> indexList = new ArrayList<String>(0);
		for (ClientCatalogItem item : ClientCatalog
				.getClientCatalog(getLoggedUser()))
			indexList.add(item.getIndexName());
		return indexList;
	}

	private Client getForeignClient() throws SearchLibException,
			NamingException {
		String indexName = currentItem.getIndexName();
		if (indexName == null)
			return null;
		return ClientCatalog.getClient(indexName);
	}

	public List<String> getQueryList() throws SearchLibException,
			NamingException {
		Client client = getForeignClient();
		if (client == null)
			return null;
		return client.getRequestMap()
				.getNameList(RequestTypeEnum.SearchRequest);
	}

	private List<String> getIndexedFieldList(Client client) {
		if (client == null)
			return null;
		List<String> fieldList = new ArrayList<String>();
		client.getSchema().getFieldList().getIndexedFields(fieldList);
		return fieldList;
	}

	public List<String> getLocalFieldList() throws SearchLibException {
		return getIndexedFieldList(getClient());
	}

	public List<String> getForeignFieldList() throws SearchLibException,
			NamingException {
		return getIndexedFieldList(getForeignClient());
	}

	public void onRemove(Component comp) throws SearchLibException {
		JoinItem item = (JoinItem) getRecursiveComponentAttribute(comp,
				"joinItem");
		((SearchRequest) getRequest()).getJoinList().remove(item);
		onCancel();
	}

	public void onSelectIndex() {
		if (Logging.isDebug)
			Logging.debug("ON SELECT");
	}
}
