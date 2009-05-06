/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.SchemaField;

public class CollapsingController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4269436181925550723L;

	private List<String> indexedFields = null;

	public CollapsingController() throws SearchLibException {
		super();
	}

	public List<String> getIndexedFields() throws SearchLibException {
		synchronized (this) {
			if (indexedFields != null)
				return indexedFields;
			indexedFields = new ArrayList<String>();
			indexedFields.add(null);
			for (SchemaField field : getClient().getSchema().getFieldList())
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

}
