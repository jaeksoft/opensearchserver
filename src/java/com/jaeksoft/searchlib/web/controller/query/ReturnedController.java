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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.SchemaField;

public class ReturnedController extends QueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9118404394554950556L;

	public ReturnedController() throws SearchLibException {
		super();
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

}
