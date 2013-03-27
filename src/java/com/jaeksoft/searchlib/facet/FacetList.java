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

package com.jaeksoft.searchlib.facet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FacetList implements Iterable<Facet> {

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

	public void add(Facet facet) {
		facetList.add(facet);
		facetMap.put(facet.facetField.getName(), facet);
	}

	public List<Facet> getList() {
		return facetList;
	}

}
