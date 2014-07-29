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

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchField;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery.SearchField.Mode;

@AfterCompose(superclass = true)
public class SearchFieldsController extends AbstractQueryController {

	private transient String selectedSearchField;

	private transient List<String> searchableFields;

	public SearchFieldsController() throws SearchLibException {
		super(RequestTypeEnum.SearchFieldRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSearchField = null;
		searchableFields = null;
	}

	public List<String> getSearchableFields() throws SearchLibException {
		synchronized (this) {
			if (searchableFields != null)
				return searchableFields;
			Client client = getClient();
			if (client == null)
				return null;
			searchableFields = new ArrayList<String>(0);
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkIndexed(Indexed.YES))
					searchableFields.add(field.getName());
			return searchableFields;
		}
	}

	public void setSelectedSearchField(String value) {
		synchronized (this) {
			selectedSearchField = value;
		}
	}

	public String getSelectedSearchField() {
		synchronized (this) {
			return selectedSearchField;
		}
	}

	private SearchFieldRequest getSearchFieldRequest()
			throws SearchLibException {
		return (SearchFieldRequest) getRequest();
	}

	public Mode[] getSearchFieldModes() {
		return Mode.values();
	}

	@Command
	@NotifyChange("*")
	public void onSearchFieldAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSearchField == null)
				return;
			getSearchFieldRequest().add(
					new SearchField(selectedSearchField, Mode.PATTERN, 1.0,
							1.0, null));
		}
	}

	@Command
	@NotifyChange("*")
	public void onSearchFieldRemove(
			@BindingParam("searchfield") SearchField searchField)
			throws SearchLibException {
		synchronized (this) {
			getSearchFieldRequest().remove(searchField);
		}
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reset();
	}
}
