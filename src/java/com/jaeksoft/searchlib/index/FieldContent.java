/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.index;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FieldContent implements Externalizable, Collecter<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4734981884898319100L;

	private String field;
	private List<String> values;

	public FieldContent() {
		values = new ArrayList<String>();
	}

	public FieldContent(String field) {
		this();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void add(String value) {
		values.add(value);
	}

	public void clear() {
		values.clear();
	}

	public String getValue(int pos) {
		if (values == null)
			return null;
		return values.get(pos);
	}

	public void setValue(int pos, String value) {
		values.set(pos, value);
	}

	public List<String> getValues() {
		return values;
	}

	public void remove(int index) {
		values.remove(index);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		field = External.readObject(in);
		External.readCollection(in, this);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObject(field, out);
		External.writeCollection(values, out);

	}

	public void addObject(String value) {
		add(value);
	}

}
