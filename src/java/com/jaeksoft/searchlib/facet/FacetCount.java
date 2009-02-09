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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FacetCount implements Serializable, Iterable<FacetItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5141078830351811256L;
	private Map<String, FacetItem> facetMap;
	private transient Object[] array = null;

	public FacetCount() {
		facetMap = new HashMap<String, FacetItem>();
	}

	protected FacetCount(String[] terms, int[] counts, int minCount) {
		this();
		int i = 0;
		for (int count : counts) {
			if (count >= minCount) {
				String term = terms[i];
				if (term != null) {
					FacetItem facetItem = new FacetItem(term, count);
					facetMap.put(term, facetItem);
				}
			}
			i++;
		}
	}

	protected void sum(FacetCount facetCount) {
		if (facetCount == null)
			return;
		for (FacetItem facetItem : facetCount) {
			if (facetItem.term == null)
				continue;
			FacetItem currentFacetItem = facetMap.get(facetItem.term);
			if (currentFacetItem != null) {
				currentFacetItem.count += facetItem.count;
				return;
			}
			facetMap.put(facetItem.term, facetItem);
		}
	}

	protected void expungeLowCount(int minCount) {
		Iterator<FacetItem> iterator = iterator();
		while (iterator.hasNext())
			if (iterator.next().count < minCount)
				iterator.remove();
	}

	private Object[] getArray() {
		if (array != null)
			return array;
		array = facetMap.values().toArray();
		return array;
	}

	protected FacetItem get(int i) {
		return (FacetItem) getArray()[i];
	}

	protected int size() {
		return getArray().length;
	}

	public Iterator<FacetItem> iterator() {
		return facetMap.values().iterator();
	}

}
