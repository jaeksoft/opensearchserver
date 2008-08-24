/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;

public class FacetList extends AbstractList<Facet> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2891562911711846847L;

	private ArrayList<Facet> facetList;
	private HashMap<String, Facet> facetMap;

	public FacetList() {
		this.facetMap = new HashMap<String, Facet>();
		this.facetList = new ArrayList<Facet>();
	}

	@Override
	public boolean add(Facet facet) {
		if (!this.facetList.add(facet))
			return false;
		this.facetMap.put(facet.facetField.getName(), facet);
		return true;
	}

	@Override
	public Facet get(int index) {
		return this.facetList.get(index);
	}

	@Override
	public int size() {
		return this.facetList.size();
	}

	public Facet getByField(String fieldName) {
		return facetMap.get(fieldName);
	}

}
