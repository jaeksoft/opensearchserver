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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FacetList implements Iterable<Facet>, Externalizable,
		Collecter<Facet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2891562911711846847L;

	private List<Facet> facetList;
	private transient Map<String, Facet> facetMap;

	public FacetList() {
		this.facetMap = new HashMap<String, Facet>();
		this.facetList = new ArrayList<Facet>();
	}

	public Facet getByField(String fieldName) {
		return facetMap.get(fieldName);
	}

	public Iterator<Facet> iterator() {
		return facetList.iterator();
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(facetList, out);
	}

	public void addObject(Facet facet) {
		facetList.add(facet);
		facetMap.put(facet.facetField.getName(), facet);
	}

}
