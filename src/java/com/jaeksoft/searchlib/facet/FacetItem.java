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

package com.jaeksoft.searchlib.facet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.jaeksoft.searchlib.util.External;

public class FacetItem implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1293498473522757010L;

	protected String term;

	protected int count;

	public FacetItem() {
	}

	public FacetItem(String term, int count) {
		this.term = term;
		this.count = count;
	}

	public String getTerm() {
		return term;
	}

	public int getCount() {
		return count;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		term = External.readUTF(in);
		count = in.readInt();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeUTF(term, out);
		out.writeInt(count);
	}

}
