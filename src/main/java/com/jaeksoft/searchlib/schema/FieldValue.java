/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class FieldValue extends AbstractField<FieldValue> {

	private static final long serialVersionUID = -6131981428734961071L;

	private List<FieldValueItem> valueList;

	public FieldValue() {
		valueList = null;
	}

	public FieldValue(String name) {
		super(name);
		valueList = null;
	}

	@Override
	public FieldValue duplicate() {
		return new FieldValue(this);
	}

	public FieldValue(FieldValue field) {
		this(field.name);
		this.valueList = field.valueList == null ? null
				: new ArrayList<FieldValueItem>(field.valueList);
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
		if (valueList == null)
			return 0;
		return valueList.size();
	}

	public List<FieldValueItem> getValueList() {
		return valueList;
	}

	public List<String> getValueStringList() {
		if (valueList == null)
			return null;
		List<String> values = new ArrayList<String>(valueList.size());
		for (FieldValueItem valueItem : valueList)
			if (valueItem.value != null)
				values.add(valueItem.value);
		return values;
	}

	public String[] getNewStringArray() {
		if (valueList == null || valueList.size() == 0)
			return null;
		String[] values = new String[valueList.size()];
		int i = 0;
		for (FieldValueItem value : valueList)
			values[i++] = value.getValue();
		return values;
	}

	final public void populate(final Collection<String> list) {
		if (valueList == null || valueList.size() == 0)
			return;
		for (FieldValueItem fvi : valueList)
			list.add(fvi.value);
	}

	public void setValues(List<FieldValueItem> values) {
		if (values == null || values.size() == 0) {
			valueList = null;
			return;
		}
		if (valueList == null)
			valueList = new ArrayList<FieldValueItem>(values.size());
		else
			valueList.clear();
		valueList.addAll(values);
	}

	public void setValues(FieldValueItem[] values) {
		if (values == null || values.length == 0) {
			valueList = null;
			return;
		}
		if (valueList == null)
			valueList = new ArrayList<FieldValueItem>(values.length);
		else
			valueList.clear();
		for (FieldValueItem value : values)
			valueList.add(value);
	}

	public void addValues(FieldValueItem... values) {
		if (values != null)
			for (FieldValueItem value : values)
				valueList.add(value);
	}

	public void addValue(FieldValueItem value) {
		if (value == null)
			return;
		if (valueList == null)
			valueList = new ArrayList<FieldValueItem>(1);
		valueList.add(value);
	}

	public void addIfStringDoesNotExist(FieldValueItem value) {
		if (value == null)
			return;
		for (FieldValueItem valueItem : valueList)
			if (value.equals(valueItem))
				return;
		valueList.add(value);
	}

	public void addIfStringDoesNotExist(List<FieldValueItem> values) {
		if (CollectionUtils.isEmpty(valueList)) {
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
				sb.append(valueList.get(0).getValue());
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
