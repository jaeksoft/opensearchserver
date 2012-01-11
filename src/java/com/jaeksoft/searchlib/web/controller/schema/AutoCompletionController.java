/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.schema;

import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AutoCompletionController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2654142296653263306L;

	private SchemaField field;

	public AutoCompletionController() throws SearchLibException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void reset() throws SearchLibException {
		// TODO Auto-generated method stub

	}

	public List<SchemaField> getFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getSchema().getFieldList().getSortedList();
		}
	}

	/**
	 * @return the field
	 */
	public SchemaField getField() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(SchemaField field) {
		this.field = field;
	}

}
