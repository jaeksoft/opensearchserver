/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseMode;
import com.jaeksoft.searchlib.schema.SchemaField;

public class CollapsingController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4269436181925550723L;

	private transient List<String> indexedFields;

	public CollapsingController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		indexedFields = null;
	}

	public CollapseMode[] getCollapseModes() {
		return CollapseMode.values();
	}

	public List<String> getIndexedFields() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (indexedFields != null)
				return indexedFields;
			indexedFields = new ArrayList<String>();
			indexedFields.add(null);
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.isIndexed())
					indexedFields.add(field.getName());
			return indexedFields;
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			indexedFields = null;
			super.reloadPage();
		}
	}

	@Override
	public void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}
}
