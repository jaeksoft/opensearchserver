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

package com.jaeksoft.searchlib.index;

import org.apache.commons.lang3.ArrayUtils;

import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FieldContent implements Collecter<FieldValueItem> {

	private String field;
	private FieldValueItem[] values;

	public FieldContent() {
		values = FieldValueItem.emptyArray;
	}

	public FieldContent(String field) {
		this();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void add(FieldValueItem value) {
		values = ArrayUtils.add(values, value);
	}

	public boolean checkIfAlreadyHere(FieldContent fc2) {
		int fc2size = fc2.values.length;
		if (values.length < fc2size)
			return false;
		int i = values.length - fc2size;
		for (FieldValueItem v : fc2.values)
			if (!v.equals(values[i++]))
				return false;
		return true;
	}

	public void add(FieldContent fc2) {
		values = ArrayUtils.addAll(values, fc2.values);
	}

	@Override
	public void addObject(FieldValueItem valueItem) {
		add(valueItem);
	}

	public void clear() {
		values = FieldValueItem.emptyArray;
	}

	public FieldValueItem getValue(int pos) {
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos];
	}

	public void setValue(int pos, FieldValueItem value) {
		values[pos] = value;
	}

	public FieldValueItem[] getValues() {
		return values;
	}

	public String getMergedValues(String separator) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (FieldValueItem item : values) {
			if (first)
				first = false;
			else
				sb.append(separator);
			sb.append(item.getValue());
		}
		return sb.toString();
	}

	public String getMergedValues(int max, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (FieldValueItem item : values) {
			if (first)
				first = false;
			else
				sb.append(separator);
			sb.append(item.getValue());
			if (sb.length() > max) {
				sb.setLength(max);
				break;
			}
		}
		return sb.toString();
	}

	public void remove(int index) {
		values = ArrayUtils.remove(values, index);
	}

	public boolean isEquals(FieldContent fc) {
		if (!field.equals(fc.getField()))
			return false;
		if (values.length != fc.values.length)
			return false;
		int i = 0;
		for (FieldValueItem v1 : values) {
			FieldValueItem v2 = fc.values[i++];
			if (v1 == null) {
				if (v2 != null)
					return false;
				else
					continue;
			}
			if (!v1.equals(v2))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(field);
		sb.append('(');
		sb.append(values.length);
		sb.append(')');
		if (values.length > 0) {
			sb.append(':');
			sb.append(values[0].getValue());
		}
		return sb.toString();
	}

	public String toLabel() {
		StringBuffer sb = new StringBuffer();
		sb.append(field);
		sb.append('(');
		sb.append(values.length);
		sb.append(')');
		return sb.toString();
	}
}
