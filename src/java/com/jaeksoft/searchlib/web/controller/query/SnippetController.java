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
import org.zkoss.zul.RowRenderer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.snippet.SnippetField;

public class SnippetController extends SearchRequestController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1641413871487856522L;

	private transient String selectedSnippet;

	private transient List<String> snippetFieldLeft;

	private transient RowRenderer rowRenderer;

	public SnippetController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSnippet = null;
		snippetFieldLeft = null;
		rowRenderer = null;
	}

	public RowRenderer getSnippetFieldRenderer() {
		synchronized (this) {
			if (rowRenderer != null)
				return rowRenderer;
			rowRenderer = new SnippetFieldRenderer();
			return rowRenderer;
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getSnippetFieldLeft();
			if (list == null)
				return false;
			return list.size() > 0;
		}
	}

	public List<String> getSnippetFieldLeft() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			SearchRequest request = (SearchRequest) getRequest();
			if (request == null)
				return null;
			if (snippetFieldLeft != null)
				return snippetFieldLeft;
			snippetFieldLeft = new ArrayList<String>();
			FieldList<SnippetField> snippetFields = request
					.getSnippetFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.isStored())
					if (field.getTermVector() == TermVector.POSITIONS_OFFSETS)
						if (snippetFields.get(field.getName()) == null) {
							if (selectedSnippet == null)
								selectedSnippet = field.getName();
							snippetFieldLeft.add(field.getName());
						}
			return snippetFieldLeft;
		}
	}

	public void onSnippetRemove(Event event) throws SearchLibException {
		synchronized (this) {
			SnippetField field = (SnippetField) event.getData();
			((SearchRequest) getRequest()).getSnippetFieldList().remove(field);
			reloadPage();
		}
	}

	public void setSelectedSnippet(String value) {
		synchronized (this) {
			selectedSnippet = value;
		}
	}

	public String getSelectedSnippet() {
		synchronized (this) {
			return selectedSnippet;
		}
	}

	public void onSnippetAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSnippet == null)
				return;
			((SearchRequest) getRequest()).getSnippetFieldList().add(
					new SnippetField(selectedSnippet));
			reloadPage();
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			snippetFieldLeft = null;
			selectedSnippet = null;
			super.reloadPage();
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}
}
