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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class FieldValue extends AbstractField<FieldValue> {

	private static final long serialVersionUID = -6131981428734961071L;

	private FieldValueItem[] valueArray;

	public FieldValue() {
	}

	public FieldValue(String name) {
		super(name);
		valueArray = FieldValueItem.emptyArray;
	}

	@Override
	public FieldValue duplicate() {
		return new FieldValue(this);
	}

	public FieldValue(FieldValue field) {
		this(field.name);
		this.valueArray = field.valueArray;
	}

	public FieldValue(String fieldName, FieldValueItem[] values) {
		super(fieldName);
		setValues(values);
	}

	public FieldValue(String fieldName, List<FieldValueItem> values) {
		super(fieldName);
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

	public String[] getNewStringArray() {
		if (valueArray == null)
			return null;
		if (valueArray.length == 0)
			return null;
		String[] values = new String[valueArray.length];
		int i = 0;
		for (FieldValueItem value : valueArray)
			values[i++] = value.getValue();
		return values;
	}

	final public void populate(final Collection<String> list) {
		if (valueArray == null)
			return;
		if (valueArray.length == 0)
			return;
		for (FieldValueItem fvi : valueArray)
			list.add(fvi.value);
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

	public void addValues(FieldValueItem... value) {
		valueArray = ArrayUtils.addAll(valueArray, value);
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
		StringBuilder sb = new StringBuilder(name);
		int c = getValuesCount();
		if (c > 0) {
			sb.append('(');
			sb.append(c);
			sb.append(')');
			if (c == 1) {
				sb.append(' ');
				sb.append(valueArray[0].getValue());
			}
		}
		return sb.toString();
	}

	public String getLabel() {
		StringBuilder sb = new StringBuilder(name);
		sb.append('(');
		sb.append(getValuesCount());
		sb.append(')');
		return sb.toString();
	}

}
