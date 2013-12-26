/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

	public FieldContent(final String field) {
		this();
		this.field = field;
	}

	final public String getField() {
		return field;
	}

	final public void add(final FieldValueItem value) {
		values = ArrayUtils.add(values, value);
	}

	public final void addIfNotAlreadyHere(final FieldValueItem value) {
		for (FieldValueItem v : values)
			if (value.equals(v))
				return;
		add(value);
	}

	final public void addIfNotAlreadyHere(final FieldContent fc2) {
		for (FieldValueItem v : fc2.values)
			addIfNotAlreadyHere(v);
	}

	final public void add(final FieldContent fc2) {
		values = ArrayUtils.addAll(values, fc2.values);
	}

	@Override
	final public void addObject(final FieldValueItem valueItem) {
		add(valueItem);
	}

	final public void clear() {
		values = FieldValueItem.emptyArray;
	}

	final public void setValueItems(final FieldValueItem[] values) {
		this.values = values;
	}

	final public FieldValueItem getValue(final int pos) {
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos];
	}

	final public void setValue(final int pos, final FieldValueItem value) {
		values[pos] = value;
	}

	final public FieldValueItem[] getValues() {
		return values;
	}

	final public boolean hasContent() {
		return values.length > 0;
	}

	public final String getMergedValues(final String separator) {
		StringBuilder sb = new StringBuilder();
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

	final public String getMergedValues(final int max, final String separator) {
		StringBuilder sb = new StringBuilder();
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

	final public void remove(int index) {
		values = ArrayUtils.remove(values, index);
	}

	final public boolean isEquals(FieldContent fc) {
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
	final public String toString() {
		StringBuilder sb = new StringBuilder();
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

	final public String toLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(field);
		sb.append('(');
		sb.append(values.length);
		sb.append(')');
		return sb.toString();
	}

}
