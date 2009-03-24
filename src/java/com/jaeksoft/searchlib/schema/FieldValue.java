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

package com.jaeksoft.searchlib.schema;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.util.External;

public class FieldValue extends Field implements Externalizable {

	private static final long serialVersionUID = -6131981428734961071L;

	private String[] valueArray;
	private transient List<String> valueList;

	public FieldValue() {
	}

	protected FieldValue(String name) {
		super(name);
		valueArray = null;
		valueList = null;
	}

	public FieldValue(FieldValue field) {
		this(field.name);
		this.valueArray = field.valueArray;
	}

	public FieldValue(Field field, String[] values) {
		super(field.name);
		setValues(values);
	}

	public FieldValue(Field field, List<String> values) {
		super(field.name);
		setValues(values);
	}

	public int getValuesCount() {
		if (valueArray == null)
			return 0;
		return valueArray.length;
	}

	public String[] getValueArray() {
		return valueArray;
	}

	public List<String> getValueList() {
		if (valueList != null)
			return valueList;
		if (valueArray == null)
			return null;
		valueList = new ArrayList<String>();
		for (String value : valueArray)
			valueList.add(value);
		return valueList;
	}

	public void setValues(String[] values) {
		valueArray = values;
		valueList = null;
	}

	public void setValues(List<String> values) {
		if (values == null) {
			valueArray = null;
			valueList = null;
			return;
		}
		valueArray = new String[values.size()];
		values.toArray(valueArray);
		valueList = null;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		valueArray = External.readStringArray(in);
		valueList = null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		External.writeStringArray(valueArray, out);
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
