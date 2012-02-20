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
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class ReturnedController extends SearchRequestController implements
		RowRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9118404394554950556L;

	private transient String selectedReturn;

	private transient List<String> fieldLeft;

	public ReturnedController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedReturn = null;
		fieldLeft = null;
	}

	public void setSelectedReturn(String value) {
		synchronized (this) {
			selectedReturn = value;
		}
	}

	public String getSelectedReturn() {
		synchronized (this) {
			return selectedReturn;
		}
	}

	public void onReturnAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedReturn == null)
				return;
			((SearchRequest) getRequest()).getReturnFieldList().add(
					new Field(selectedReturn));
			reloadPage();
		}
	}

	public void onReturnRemove(Event event) throws SearchLibException {
		synchronized (this) {
			Field field = (Field) event.getData();
			((SearchRequest) getRequest()).getReturnFieldList().remove(field);
			reloadPage();
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getReturnFieldLeft();
			if (list == null)
				return false;
			return list.size() > 0;
		}
	}

	public List<String> getReturnFieldLeft() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (fieldLeft != null)
				return fieldLeft;
			fieldLeft = new ArrayList<String>();
			SearchRequest request = (SearchRequest) getRequest();
			if (request == null)
				return null;
			FieldList<Field> fields = request.getReturnFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.isStored() || field.isIndexed())
					if (fields.get(field.getName()) == null) {
						if (selectedReturn == null)
							selectedReturn = field.getName();
						fieldLeft.add(field.getName());
					}
			return fieldLeft;
		}
	}

	@Override
	public void render(Row row, Object data) throws Exception {
		Field field = (Field) data;
		new Label(field.getName()).setParent(row);
		Button button = new Button("Remove");
		button.addForward(null, "query", "onReturnRemove", field);
		button.setParent(row);
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			selectedReturn = null;
			fieldLeft = null;
			super.reloadPage();
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}
}
