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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckDistanceEnum;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckFieldList;

public class SpellCheckController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5132791474383971273L;

	private transient List<String> fieldLeft;

	private transient SpellCheckField currentSpellCheckField;

	private transient SpellCheckField selectedSpellCheckField;

	public SpellCheckController() throws SearchLibException {
		super(RequestTypeEnum.SpellCheckRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
		getSpellCheckFieldLeft();
		String fieldName = "";
		if (fieldLeft != null && fieldLeft.size() > 0)
			fieldName = fieldLeft.get(0);
		currentSpellCheckField = new SpellCheckField(fieldName, 0.5F, 5,
				SpellCheckDistanceEnum.LevensteinDistance);
		fieldLeft = null;
		selectedSpellCheckField = null;
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getSpellCheckFieldLeft();
			if (list == null)
				return false;
			return list.size() > 0;
		}
	}

	public List<String> getSpellCheckFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (fieldLeft != null)
				return fieldLeft;
			Client client = getClient();
			if (client == null)
				return null;
			SpellCheckRequest request = (SpellCheckRequest) getRequest();
			if (request == null)
				return null;
			SpellCheckFieldList spellCheckFieldList = request
					.getSpellCheckFieldList();
			fieldLeft = new ArrayList<String>();
			for (SchemaField field : client.getSchema().getFieldList()) {
				String fieldName = field.getName();
				if (selectedSpellCheckField != null
						&& selectedSpellCheckField.getName().equals(fieldName)) {
					fieldLeft.add(field.getName());
					continue;
				}
				if (field.isIndexed())
					if (spellCheckFieldList.get(fieldName) == null)
						fieldLeft.add(field.getName());
			}
			return fieldLeft;
		}
	}

	public void onFieldRemove(Event event) throws SearchLibException {
		synchronized (this) {
			event = getOriginalEvent(event);
			SpellCheckField spellCheckField = (SpellCheckField) event
					.getTarget().getAttribute("scFieldItem");
			((SpellCheckRequest) getRequest()).getSpellCheckFieldList().remove(
					spellCheckField.getName());
			onCancel();
		}
	}

	public void onFieldAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSpellCheckField != null)
				selectedSpellCheckField.copyFrom(currentSpellCheckField);
			else
				((SpellCheckRequest) getRequest()).getSpellCheckFieldList()
						.put(currentSpellCheckField);
			onCancel();
		}
	}

	public void onCancel() throws SearchLibException {
		synchronized (this) {
			reset();
			reloadPage();
		}
	}

	@Override
	public void reloadPage() throws SearchLibException {
		synchronized (this) {
			super.reloadPage();
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}

	public SpellCheckDistanceEnum[] getStringDistanceList() {
		return SpellCheckDistanceEnum.values();
	}

	public SpellCheckField getCurrent() {
		return currentSpellCheckField;
	}

	/**
	 * @return the selectedSpellCheckField
	 */
	public SpellCheckField getSelected() {
		return selectedSpellCheckField;
	}

	/**
	 * @param selectedSpellCheckField
	 *            the selectedSpellCheckField to set
	 * @throws SearchLibException
	 */
	public void setSelected(SpellCheckField selectedSpellCheckField)
			throws SearchLibException {
		this.selectedSpellCheckField = selectedSpellCheckField;
		this.currentSpellCheckField = new SpellCheckField(
				selectedSpellCheckField);
		fieldLeft = null;
		reloadPage();
	}

	public boolean isSelection() {
		return this.selectedSpellCheckField != null;
	}

	public boolean isNoSelection() {
		return !isSelection();
	}
}
