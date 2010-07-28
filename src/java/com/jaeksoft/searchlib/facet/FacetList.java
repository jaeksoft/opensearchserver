/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.facet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		this.facetMap = new TreeMap<String, Facet>();
		this.facetList = new ArrayList<Facet>();
	}

	public Facet getByField(String fieldName) {
		return facetMap.get(fieldName);
	}

	@Override
	public Iterator<Facet> iterator() {
		return facetList.iterator();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(facetList, out);
	}

	@Override
	public void addObject(Facet facet) {
		facetList.add(facet);
		facetMap.put(facet.facetField.getName(), facet);
	}

	public List<Facet> getList() {
		return facetList;
	}

}
