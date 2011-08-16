/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FieldContent implements Externalizable, Collecter<FieldValueItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4734981884898319100L;

	private String field;
	private List<FieldValueItem> values;

	public FieldContent() {
		values = new ArrayList<FieldValueItem>();
	}

	public FieldContent(String field) {
		this();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void add(FieldValueItem value) {
		values.add(value);
	}

	public boolean checkIfAlreadyHere(FieldContent fc2) {
		int fc2size = fc2.values.size();
		if (values.size() < fc2size)
			return false;
		int i = values.size() - fc2size;
		for (FieldValueItem v : fc2.values)
			if (!v.equals(values.get(i++)))
				return false;
		return true;
	}

	public void add(FieldContent fc2) {
		values.addAll(fc2.values);
	}

	@Override
	public void addObject(FieldValueItem valueItem) {
		values.add(valueItem);
	}

	public void clear() {
		values.clear();
	}

	public FieldValueItem getValue(int pos) {
		if (values == null)
			return null;
		return values.get(pos);
	}

	public void setValue(int pos, FieldValueItem value) {
		values.set(pos, value);
	}

	public List<FieldValueItem> getValues() {
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
		values.remove(index);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		field = External.readObject(in);
		External.readCollection(in, this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObject(field, out);
		External.writeCollection(values, out);
	}

	public boolean isEquals(FieldContent fc) {
		if (!field.equals(fc.getField()))
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

}
