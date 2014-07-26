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

package com.jaeksoft.searchlib.index;

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FieldContent implements Collecter<FieldValueItem> {

	private String field;
	private List<FieldValueItem> values;

	public FieldContent() {
		values = null;
	}

	public FieldContent(final String field) {
		this();
		this.field = field;
	}

	final public String getField() {
		return field;
	}

	final public void add(final FieldValueItem value) {
		if (values == null)
			values = new ArrayList<FieldValueItem>(1);
		values.add(value);
	}

	public final void addIfNotAlreadyHere(final FieldValueItem value) {
		if (values != null)
			for (FieldValueItem v : values)
				if (value.equals(v))
					return;
		add(value);
	}

	final public void addIfNotAlreadyHere(final FieldContent fc2) {
		if (fc2.values != null)
			for (FieldValueItem v : fc2.values)
				addIfNotAlreadyHere(v);
	}

	final public void add(final FieldContent fc2) {
		if (fc2.values == null)
			return;
		if (values == null)
			values = new ArrayList<FieldValueItem>(1);
		values.addAll(fc2.values);
	}

	@Override
	final public void addObject(final FieldValueItem valueItem) {
		add(valueItem);
	}

	final public void clear() {
		values = null;
	}

	final public void setValueItems(List<FieldValueItem> valueItems) {
		if (valueItems == null)
			return;
		if (values == null)
			values = new ArrayList<FieldValueItem>();
		else
			values.clear();
		values.addAll(valueItems);
	}

	final public FieldValueItem getValue(final int pos) {
		if (values == null)
			return null;
		if (pos >= values.size())
			return null;
		return values.get(pos);
	}

	final public void setValue(final int pos, final FieldValueItem value) {
		values.set(pos, value);
	}

	final public List<FieldValueItem> getValues() {
		return values;
	}

	final public int getValueCount() {
		if (values == null)
			return 0;
		return values.size();
	}

	final public boolean hasContent() {
		return getValueCount() > 0;
	}

	public final String getMergedValues(final String separator) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if (values != null) {
			for (FieldValueItem item : values) {
				if (first)
					first = false;
				else
					sb.append(separator);
				sb.append(item.getValue());
			}
		}
		return sb.toString();
	}

	final public String getMergedValues(final int max, final String separator) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if (values != null) {
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
		}
		return sb.toString();
	}

	final public void remove(int index) {
		values.remove(index);
	}

	final public boolean isEquals(FieldContent fc) {
		if (!field.equals(fc.getField()))
			return false;
		if (values == null) {
			if (fc.values == null)
				return true;
			else
				return false;
		}
		if (fc.values == null)
			return false;
		if (values.size() != fc.values.size())
			return false;
		int i = 0;
		for (FieldValueItem v1 : values) {
			FieldValueItem v2 = fc.values.get(i++);
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
		sb.append(values == null ? 0 : values.size());
		sb.append(')');
		if (values != null && values.size() > 0) {
			sb.append(':');
			sb.append(values.get(0).getValue());
		}
		return sb.toString();
	}

	final public String toLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(field);
		sb.append('(');
		sb.append(values == null ? 0 : values.size());
		sb.append(')');
		return sb.toString();
	}

}
