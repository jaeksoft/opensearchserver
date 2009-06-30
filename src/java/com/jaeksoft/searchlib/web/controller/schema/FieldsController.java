/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.web.controller.schema;

import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class FieldsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8974865544547689492L;

	private SchemaField field;

	public FieldsController() throws SearchLibException {
		super();
		field = new SchemaField();
	}

	public SchemaField getField() {
		return field;
	}

	public Stored[] getStoredList() {
		return Stored.values();
	}

	public Indexed[] getIndexedList() {
		return Indexed.values();
	}

	public TermVector[] getTermVectorList() {
		return TermVector.values();
	}

	public List<SchemaField> getList() throws SearchLibException {
		return getClient().getSchema().getFieldList().getList();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return getClient().getSchema().getFieldList().get(field.getName()) == null ? "Create a new field"
				: "Edit the field " + field.getName();
	}

}
