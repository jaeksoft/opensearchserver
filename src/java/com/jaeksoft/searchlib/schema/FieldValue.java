/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.util.ArrayList;
import java.util.List;

public class FieldValue extends Field {

	private static final long serialVersionUID = -6131981428734961071L;

	private FieldValueItem[] valueArray;
	private transient List<FieldValueItem> valueList;

	private final static FieldValueItem[] emptyValueArray = new FieldValueItem[0];

	public FieldValue() {
	}

	protected FieldValue(String name) {
		super(name);
		valueArray = emptyValueArray;
		valueList = null;
	}

	public FieldValue(Field field) {
		this(field.name);

	}

	public FieldValue(FieldValue field) {
		this(field.name);
		this.valueArray = field.valueArray;
	}

	public FieldValue(Field field, FieldValueItem[] values) {
		super(field.name);
		setValues(values);
	}

	public FieldValue(Field field, List<FieldValueItem> values) {
		super(field.name);
		setValues(values);
	}

	public int getValuesCount() {
		if (valueArray == null)
			return 0;
		return valueArray.length;
	}

	public FieldValueItem[] getValueArray() {
		return valueArray;
	}

	public List<FieldValueItem> getValueList() {
		if (valueList != null)
			return valueList;
		if (valueArray == null)
			return null;
		valueList = new ArrayList<FieldValueItem>();
		for (FieldValueItem value : valueArray)
			valueList.add(value);
		return valueList;
	}

	public void setValues(FieldValueItem[] values) {
		valueArray = values;
		valueList = null;
	}

	public void setValues(List<FieldValueItem> values) {
		if (values == null) {
			valueArray = emptyValueArray;
			valueList = null;
			return;
		}
		valueArray = new FieldValueItem[values.size()];
		values.toArray(valueArray);
		valueList = null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(name);
		int c = getValuesCount();
		if (c > 0) {
			sb.append('(');
			sb.append(c);
			sb.append(')');
		}
		return sb.toString();
	}
}
