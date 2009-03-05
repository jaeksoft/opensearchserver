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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class FacetController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5748867672639969504L;

	private String selectedFacet = null;

	private List<String> fieldLeft = null;

	private RowRenderer rowRenderer = null;

	public FacetController() throws SearchLibException {
		super();
	}

	public RowRenderer getFacetFieldRenderer() {
		synchronized (this) {
			if (rowRenderer != null)
				return rowRenderer;
			rowRenderer = new FacetFieldRenderer();
			return rowRenderer;
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			return getFacetFieldLeft().size() > 0;
		}
	}

	public List<String> getFacetFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (fieldLeft != null)
				return fieldLeft;
			fieldLeft = new ArrayList<String>();
			FieldList<FacetField> facetFields = getRequest()
					.getFacetFieldList();
			for (SchemaField field : getClient().getSchema().getFieldList())
				if (field.isIndexed())
					if (facetFields.get(field.getName()) == null) {
						if (selectedFacet == null)
							selectedFacet = field.getName();
						fieldLeft.add(field.getName());
					}
			return fieldLeft;
		}
	}

	public void onFacetRemove(Event event) throws SearchLibException {
		synchronized (this) {
			FacetField facetField = (FacetField) event.getData();
			getRequest().getFacetFieldList().remove(facetField);
			reloadPage();
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

	public void onFacetAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedFacet == null)
				return;
			getRequest().getFacetFieldList().add(
					new FacetField(selectedFacet, 0, false));
			reloadPage();
		}
	}

	@Override
	protected void reloadPage() {
		synchronized (this) {
			fieldLeft = null;
			selectedFacet = null;
			super.reloadPage();
		}
	}

}
