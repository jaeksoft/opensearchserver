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

package com.jaeksoft.searchlib.schema;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class FieldValue extends Field {

	private static final long serialVersionUID = -6131981428734961071L;

	private FieldValueItem[] valueArray;

	public FieldValue() {
	}

	protected FieldValue(String name) {
		super(name);
		valueArray = FieldValueItem.emptyArray;
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

	public void setValues(List<FieldValueItem> values) {
		if (values == null || values.size() == 0) {
			valueArray = FieldValueItem.emptyArray;
			return;
		}
		valueArray = new FieldValueItem[values.size()];
		values.toArray(valueArray);
	}

	public void setValues(FieldValueItem[] values) {
		valueArray = values;
	}

	public void addValues(FieldValueItem[] values) {
		if (valueArray == null) {
			setValues(values);
			return;
		}
		valueArray = ArrayUtils.addAll(valueArray, values);
	}

	public void addIfStringDoesNotExist(FieldValueItem value) {
		if (value == null)
			return;
		for (FieldValueItem valueItem : valueArray)
			if (value.equals(valueItem))
				return;
		valueArray = ArrayUtils.add(valueArray, value);
	}

	public void addIfStringDoesNotExist(FieldValueItem[] values) {
		if (valueArray == null) {
			setValues(values);
			return;
		}
		for (FieldValueItem value : values)
			addIfStringDoesNotExist(value);
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
