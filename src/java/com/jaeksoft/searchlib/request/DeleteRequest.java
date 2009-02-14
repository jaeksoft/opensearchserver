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

package com.jaeksoft.searchlib.request;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;

public class DeleteRequest implements Externalizable, Iterable<String>,
		Collecter<String> {

	private Collection<String> uniqFields;

	public DeleteRequest() {
		uniqFields = new ArrayList<String>();
	}

	public DeleteRequest(Collection<String> uniqFields) {
		this.uniqFields = uniqFields;
	}

	public Collection<String> getCollection() {
		return uniqFields;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(uniqFields, out);
	}

	public Iterator<String> iterator() {
		return uniqFields.iterator();
	}

	public void addObject(String uniqField) {
		uniqFields.add(uniqField);
	}

}
