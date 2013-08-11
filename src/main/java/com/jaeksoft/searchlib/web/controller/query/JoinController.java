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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterAbstract.FilterType;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

public class JoinController extends AbstractQueryController {

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

	public FilterType[] getFilterTypeList() {
		return FilterAbstract.FilterType.values();
	}

	@NotifyChange("*")
	public void setCurrentIndexName(String indexName) {
		currentItem.setIndexName(indexName);
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

	@NotifyChange("*")
	public void setSelected(JoinItem item) {
		this.selectedItem = item;
		this.currentItem = new JoinItem(item);
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		reset();
	}

	public boolean isSelection() {
		return selectedItem != null;
	}

	public boolean isNotSelection() {
		return !isSelection();
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException {
		if (selectedItem != null)
			currentItem.copyTo(selectedItem);
		else
			((AbstractSearchRequest) getRequest()).getJoinList().add(
					currentItem);
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
		List<String> queryList = new ArrayList<String>(0);
		Client client = getForeignClient();
		if (client == null)
			return queryList;
		client.getRequestMap().getNameList(queryList,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
		return queryList;
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

	@Command
	@NotifyChange("*")
	public void onRemove(@BindingParam("joinItem") JoinItem item)
			throws SearchLibException {
		((AbstractSearchRequest) getRequest()).getJoinList().remove(item);
		onCancel();
	}

}
