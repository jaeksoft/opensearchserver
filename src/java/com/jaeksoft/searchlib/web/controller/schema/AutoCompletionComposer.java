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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.web.controller.CommonComposer;

public class AutoCompletionComposer extends CommonComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2654142296653263306L;

	private SchemaField field = null;

	private Combobox combo;

	public List<SchemaField> getFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getSchema().getFieldList().getSortedList();
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
		if (field == null && manager != null && client != null)
			field = client.getSchema().getFieldList().get(manager.getField());
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
		// TODO Auto-generated method stub

	}

	public void onBuild$window(Event event) throws SearchLibException {
		AutoCompletionManager manager = getAutoCompletionManager();
		if (manager == null)
			return;
		onSave$window(event);
		manager.startBuild();
		reloadPage();
	}

	public void onSave$window(Event event) throws SearchLibException {
		AutoCompletionManager manager = getAutoCompletionManager();
		if (manager == null || field == null)
			return;
		manager.setField(field.getName());
	}

	public void onTimer$timer() {
		reloadPage();
	}

	public void onChanging$combo(Event event) {
		Event ev = getOriginalEvent(event);
		if (!(ev instanceof InputEvent))
			return;
		InputEvent inputEvent = (InputEvent) ev;
		System.out.println(inputEvent.getValue());
	}
}
