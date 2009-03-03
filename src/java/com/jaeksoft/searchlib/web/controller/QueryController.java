/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class QueryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1985920874142697802L;

	public QueryController() throws SearchLibException {
		super();
	}

	public SearchRequest getRequest() throws SearchLibException {
		SearchRequest request = (SearchRequest) getAttribute("searchRequest",
				SESSION_SCOPE);
		if (request != null)
			return request;
		Client client = getClient();
		if (client == null)
			return null;
		request = client.getNewSearchRequest();
		setRequest(request);
		return request;
	}

	public void setRequest(SearchRequest request) {
		setAttribute("searchRequest", request, SESSION_SCOPE);
	}

	private String selectedRequestName = null;

	public void setSelectedRequest(String requestName) {
		this.selectedRequestName = requestName;
	}

	public String getSelectedRequest() {
		return selectedRequestName;
	}

	public Set<String> getRequests() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		Map<String, SearchRequest> map = client.getSearchRequestMap();
		if (map == null)
			return null;
		Set<String> set = map.keySet();
		if (selectedRequestName == null || map.get(selectedRequestName) == null) {
			Iterator<String> it = set.iterator();
			if (it.hasNext())
				setSelectedRequest(it.next());
		}
		return set;
	}

	public class CheckedField {
		private Field field;
		private boolean selected;

		private CheckedField(Field field, boolean selected) {
			this.field = field;
			this.selected = selected;
		}

		public Field getField() {
			return field;
		}

		public boolean isSelected() {
			return selected;
		}

	}

	public List<CheckedField> getReturnFieldList() throws SearchLibException {
		SearchRequest request = getRequest();
		List<CheckedField> list = new ArrayList<CheckedField>();
		for (SchemaField field : getClient().getSchema().getFieldList())
			if (field.isStored())
				list.add(new CheckedField(field, request != null ? request
						.getReturnFieldList().get(field.getName()) != null
						: false));
		return list;
	}

	public void onLoadRequest() throws SearchLibException {
		setRequest(getClient().getNewSearchRequest(selectedRequestName));
		reloadPage();
	}

	public RowRenderer getFacetFieldRenderer() {
		return new FacetFieldRenderer();
	}

	public List<String> getFacetFieldLeft() throws SearchLibException {
		List<String> list = new ArrayList<String>();
		FieldList<FacetField> facetFields = getRequest().getFacetFieldList();
		for (SchemaField field : getClient().getSchema().getFieldList())
			if (field.isIndexed())
				if (facetFields.get(field.getName()) == null) {
					if (selectedFacet == null)
						selectedFacet = field.getName();
					list.add(field.getName());
				}
		return list;
	}

	public void onFacetRemove(Event event) throws SearchLibException {
		FacetField facetField = (FacetField) event.getData();
		getRequest().getFacetFieldList().remove(facetField);
		reloadPage();
	}

	private String selectedFacet = null;

	public void setSelectedFacet(String value) {
		selectedFacet = value;
	}

	public String getSelectedFacet() {
		return selectedFacet;
	}

	public void onFacetAdd() throws SearchLibException {
		System.out.println(selectedFacet);
		getRequest().getFacetFieldList().add(
				new FacetField(selectedFacet, 0, false));
		selectedFacet = null;
		reloadPage();
	}
}
