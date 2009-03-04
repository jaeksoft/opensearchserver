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
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class HighlightedController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1641413871487856522L;

	private String selectedHighlight = null;

	private List<String> highlightFieldLeft = null;

	private RowRenderer rowRenderer = null;

	public HighlightedController() throws SearchLibException {
		super();
	}

	public RowRenderer getHighlightFieldRenderer() {
		synchronized (this) {
			if (rowRenderer != null)
				return rowRenderer;
			rowRenderer = new HighlightFieldRenderer();
			return rowRenderer;
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			return getHighlightFieldLeft().size() > 0;
		}
	}

	public List<String> getHighlightFieldLeft() throws SearchLibException {
		synchronized (this) {
			if (highlightFieldLeft != null)
				return highlightFieldLeft;
			highlightFieldLeft = new ArrayList<String>();
			FieldList<HighlightField> highlightFields = getRequest()
					.getHighlightFieldList();
			for (SchemaField field : getClient().getSchema().getFieldList())
				if ("positions_offsets".equals(field.getTermVectorLabel()))
					if (highlightFields.get(field.getName()) == null) {
						if (selectedHighlight == null)
							selectedHighlight = field.getName();
						highlightFieldLeft.add(field.getName());
					}
			return highlightFieldLeft;
		}
	}

	public void onHighlightRemove(Event event) throws SearchLibException {
		synchronized (this) {
			HighlightField field = (HighlightField) event.getData();
			getRequest().getHighlightFieldList().remove(field);
			reloadPage();
		}
	}

	public void setSelectedHighlight(String value) {
		synchronized (this) {
			selectedHighlight = value;
		}
	}

	public String getSelectedHighlight() {
		synchronized (this) {
			return selectedHighlight;
		}
	}

	public void onHighlightAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedHighlight == null)
				return;
			getRequest().getHighlightFieldList().add(
					new HighlightField(selectedHighlight));
			reloadPage();
		}
	}

	@Override
	protected void reloadPage() {
		highlightFieldLeft = null;
		selectedHighlight = null;
		super.reloadPage();
	}

}
