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
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;

public class SnippetController extends AbstractQueryController {

	private transient String selectedSnippet;

	private transient List<String> snippetFieldLeft;

	public SnippetController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSnippet = null;
		snippetFieldLeft = null;
	}

	private final static String[] fragmenterList = { "NoFragmenter",
			"SentenceFragmenter" };

	final public String[] getFragmenterList() {
		return fragmenterList;
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
			AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
			if (request == null)
				return null;
			if (snippetFieldLeft != null)
				return snippetFieldLeft;
			snippetFieldLeft = new ArrayList<String>();
			SnippetFieldList snippetFields = request.getSnippetFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkStored(Stored.YES, Stored.COMPRESS))
					if (field.getTermVector() == TermVector.POSITIONS_OFFSETS)
						if (snippetFields.get(field.getName()) == null) {
							if (selectedSnippet == null)
								selectedSnippet = field.getName();
							snippetFieldLeft.add(field.getName());
						}
			return snippetFieldLeft;
		}
	}

	@Command
	public void onSnippetRemove(@BindingParam("field") SnippetField field)
			throws SearchLibException {
		synchronized (this) {
			((AbstractSearchRequest) getRequest()).getSnippetFieldList()
					.remove(field.getName());
			reload();
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

	@Command
	public void onSnippetAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedSnippet == null)
				return;
			((AbstractSearchRequest) getRequest()).getSnippetFieldList().put(
					new SnippetField(selectedSnippet));
			reload();
		}
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			snippetFieldLeft = null;
			selectedSnippet = null;
			super.reload();
		}
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}
}
