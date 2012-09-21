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

package com.jaeksoft.searchlib.web.controller.schema;

import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.web.controller.CommonComposer;

public class AutoCompletionComposer extends CommonComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2654142296653263306L;

	private SchemaField field = null;

	private int rows = 10;

	private Combobox combo;

	public List<SchemaField> getFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getSchema().getFieldList().getList();
		}
	}

	public AutoCompletionManager getAutoCompletionManager()
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getAutoCompletionManager();
	}

	/**
	 * @return the field
	 * @throws SearchLibException
	 */
	public SchemaField getField() throws SearchLibException {
		Client client = getClient();
		AutoCompletionManager manager = getAutoCompletionManager();
		if (field == null && manager != null && client != null) {
			String f = manager.getField();
			if (f != null && f.length() > 0)
				field = client.getSchema().getFieldList().get(f);
		}
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(SchemaField field) {
		this.field = field;
	}

	@Override
	protected void reset() throws SearchLibException {
		field = null;
		if (combo != null)
			combo.getChildren().clear();
	}

	public void onBuild$window(Event event) throws SearchLibException {
		AutoCompletionManager manager = getAutoCompletionManager();
		if (manager == null)
			return;
		onSave$window(event);
		manager.build(null, 1000, null);
		reloadPage();
	}

	public void onSave$window(Event event) throws SearchLibException {
		AutoCompletionManager manager = getAutoCompletionManager();
		if (manager == null || field == null)
			return;
		manager.setField(field.getName());
		manager.setRows(rows);
		manager.save();
	}

	public void onTimer$timer() {
		reloadPage();
	}

	public void onChanging$combo(Event event) throws SearchLibException {
		Event ev = getOriginalEvent(event);
		if (!(ev instanceof InputEvent))
			return;
		InputEvent inputEvent = (InputEvent) ev;
		AutoCompletionManager manager = getAutoCompletionManager();
		if (manager == null)
			return;
		String[] resultArray = new String[0];
		AbstractResultSearch result = manager.search(inputEvent.getValue(),
				rows);
		if (result != null) {
			if (result.getDocumentCount() > 0) {
				resultArray = new String[result.getDocumentCount()];
				int i = 0;
				for (ResultDocument resDoc : result) {
					resultArray[i++] = resDoc
							.getValueContent(
									AutoCompletionManager.autoCompletionSchemaFieldTerm,
									0);
				}
			}
		}
		ListModel listModel = new ListModelArray(resultArray);
		combo.setModel(listModel);
	}

	/**
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

}
