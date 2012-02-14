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
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckDistanceEnum;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;

public class SpellCheckController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5132791474383971273L;

	private transient String selectedField;

	private transient List<String> fieldLeft;

	private transient SpellCheckDistanceEnum stringDistance;

	public SpellCheckController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedField = null;
		fieldLeft = null;
		stringDistance = null;
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
			SearchRequest searchRequest = getRequest();
			if (searchRequest == null)
				return null;
			fieldLeft = new ArrayList<String>();
			FieldList<SpellCheckField> spellCheckFields = searchRequest
					.getSpellCheckFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.isIndexed())
					if (spellCheckFields.get(field.getName()) == null) {
						if (selectedField == null)
							selectedField = field.getName();
						fieldLeft.add(field.getName());
					}
			return fieldLeft;
		}
	}

	public void onFieldRemove(Event event) throws SearchLibException {
		synchronized (this) {
			SpellCheckField spellCheckField = (SpellCheckField) event.getData();
			getRequest().getSpellCheckFieldList().remove(spellCheckField);
			reloadPage();
		}
	}

	public void setSelectedField(String value) {
		synchronized (this) {
			selectedField = value;
		}
	}

	public String getSelectedField() {
		synchronized (this) {
			return selectedField;
		}
	}

	public void onFieldAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedField == null)
				return;
			getRequest().getSpellCheckFieldList().add(
					new SpellCheckField(selectedField, 0.5F, 5,
							SpellCheckDistanceEnum.LevensteinDistance));
			reloadPage();
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			fieldLeft = null;
			selectedField = null;
			super.reloadPage();
		}
	}

	@Override
	protected void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}

	/**
	 * @return the stringDistance
	 */
	public SpellCheckDistanceEnum getStringDistance() {
		return stringDistance;
	}

	/**
	 * @param stringDistance
	 *            the stringDistance to set
	 */
	public void setStringDistance(SpellCheckDistanceEnum stringDistance) {
		this.stringDistance = stringDistance;
	}

	public SpellCheckDistanceEnum[] getStringDistanceList() {
		return SpellCheckDistanceEnum.values();
	}

}
