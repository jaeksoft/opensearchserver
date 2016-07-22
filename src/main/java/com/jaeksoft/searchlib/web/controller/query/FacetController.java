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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.Facet.OrderByEnum;

public class FacetController extends AbstractQueryController {

	private transient String selectedFacet;

	private transient List<String> fieldLeft;

	public FacetController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFacet = null;
		fieldLeft = null;
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getFacetFieldLeft();
			if (list == null)
				return false;
			return getFacetFieldLeft().size() > 0;
		}
	}

	public List<String> getFacetFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (fieldLeft != null)
				return fieldLeft;
			Client client = getClient();
			if (client == null)
				return null;
			AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
			if (request == null)
				return null;
			fieldLeft = new ArrayList<String>();
			FacetFieldList facetFields = request.getFacetFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkIndexed(Indexed.YES))
					if (facetFields.get(field.getName()) == null) {
						if (selectedFacet == null)
							selectedFacet = field.getName();
						fieldLeft.add(field.getName());
					}
			return fieldLeft;
		}
	}

	@Command
	public void onFacetRemove(@BindingParam("facetField") FacetField facetField)
			throws SearchLibException {
		synchronized (this) {
			((AbstractSearchRequest) getRequest()).getFacetFieldList().remove(
					facetField.getName());
			reload();
		}
	}

	public void setSelectedFacet(String value) {
		synchronized (this) {
			selectedFacet = value;
		}
	}

	public String getSelectedFacet() {
		synchronized (this) {
			return selectedFacet;
		}
	}

	private final static String[] booleanValues = { "no", "yes" };

	public String[] getMultivaluedValues() {
		return booleanValues;
	}

	public OrderByEnum[] getOrderByValues() {
		return OrderByEnum.values();
	}

	public String[] getCollapsingValues() {
		return booleanValues;
	}

	@Command
	public void onFacetAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedFacet == null)
				return;
			((AbstractSearchRequest) getRequest()).getFacetFieldList().put(
					new FacetField(selectedFacet, 0, false, false, null, null,
							null));
			reload();
		}
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			fieldLeft = null;
			selectedFacet = null;
			super.reload();
		}
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}

}
