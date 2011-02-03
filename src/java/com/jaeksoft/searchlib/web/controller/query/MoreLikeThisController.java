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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class MoreLikeThisController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2872605532103762800L;

	private List<String> fieldsLeft;

	private String selectedField;

	public MoreLikeThisController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		fieldsLeft = null;
	}

	public List<String> getFields() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (fieldsLeft != null)
				return fieldsLeft;
			fieldsLeft = new ArrayList<String>();
			FieldList<Field> fields = getRequest().getMoreLikeThisFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (fields.get(field.getName()) == null) {
					if (selectedField == null)
						selectedField = field.getName();
					fieldsLeft.add(field.getName());
				}
			return fieldsLeft;
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			fieldsLeft = null;
			super.reloadPage();
		}
	}

	@Override
	protected void eventSchemaChange() throws SearchLibException {
		reloadPage();
	}
}
