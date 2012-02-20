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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Listitem;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortList;

public class SortedController extends SearchRequestController implements
		RowRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6525895947880411384L;

	private transient String selectedSort;

	private transient List<String> sortFieldLeft;

	public SortedController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSort = null;
		sortFieldLeft = null;
	}

	public void setSelectedSort(String value) {
		synchronized (this) {
			selectedSort = value;
		}
	}

	public String getSelectedSort() {
		synchronized (this) {
			return selectedSort;
		}
	}

	public void onSortAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSort == null)
				return;
			((SearchRequest) getRequest()).getSortList().add(
					new SortField(selectedSort, true));
			reloadPage();
		}
	}

	public void onSortRemove(Event event) throws SearchLibException {
		synchronized (this) {
			SortField sortField = (SortField) event.getData();
			((SearchRequest) getRequest()).getSortList().remove(sortField);
			reloadPage();
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getSortFieldLeft();
			if (list == null)
				return false;
			return list.size() > 0;
		}
	}

	public List<String> getSortFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (sortFieldLeft != null)
				return sortFieldLeft;
			Client client = getClient();
			if (client == null)
				return null;
			SearchRequest request = (SearchRequest) getRequest();
			if (request == null)
				return null;
			sortFieldLeft = new ArrayList<String>();
			FieldList<SortField> sortFields = request.getSortList()
					.getFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.isIndexed())
					if (sortFields.get(field.getName()) == null) {
						if (selectedSort == null)
							selectedSort = field.getName();
						sortFieldLeft.add(field.getName());
					}
			if (sortFields.get("score") == null) {
				sortFieldLeft.add("score");
				if (selectedSort == null)
					selectedSort = "score";
			}
			return sortFieldLeft;
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			selectedSort = null;
			sortFieldLeft = null;
			super.reloadPage();
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}

	public class DirectionListener implements EventListener {

		protected SortField sortField;

		public DirectionListener(SortField sortField) {
			this.sortField = sortField;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			Listbox listbox = (Listbox) event.getTarget();
			Listitem listitem = listbox.getSelectedItem();
			if (listitem != null) {
				SortList sortList = ((SearchRequest) getRequest())
						.getSortList();
				sortList.remove(sortField);
				sortField.setDesc(listitem.getValue().toString());
				sortList.add(sortField);
			}
		}
	}

	@Override
	public void render(Row row, Object data) throws Exception {
		SortField sortField = (SortField) data;
		new Label(sortField.getName()).setParent(row);
		Listbox listbox = new Listbox();
		listbox.setMold("select");
		listbox.appendItem("ascending", "+");
		listbox.appendItem("descending", "-");
		listbox.setSelectedIndex(sortField.isDesc() ? 1 : 0);
		listbox.addEventListener("onSelect", new DirectionListener(sortField));
		listbox.setParent(row);
		Button button = new Button("Remove");
		button.addForward(null, "sorted", "onSortRemove", sortField);
		button.setParent(row);
	}

}
