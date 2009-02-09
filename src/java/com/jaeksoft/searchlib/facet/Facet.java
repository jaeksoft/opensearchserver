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
import java.util.Iterator;

public abstract class Facet implements Serializable, Iterable<FacetItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2867011819195319222L;

	protected FacetField facetField;

	protected FacetCount facetCount;

	public Facet(FacetField facetField) {
		this.facetField = facetField;
	}

	public FacetField getFacetField() {
		return this.facetField;
	}

	public int getTermCount() {
		return facetCount.size();
	}

	public String getTerm(int i) {
		return facetCount.get(i).term;
	}

	public int getCount(int i) {
		return facetCount.get(i).count;
	}

	protected void setResult(FacetCount facetCount) {
		this.facetCount = facetCount;
	}

	public Iterator<FacetItem> iterator() {
		return facetCount.iterator();
	}
}
