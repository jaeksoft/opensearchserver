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

public abstract class Facet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected FacetField facetField;

	public Facet(FacetField facetField) {
		this.facetField = facetField;
	}

	public abstract String[] getTerms();

	public abstract int[] getCount();

	public FacetField getFacetField() {
		return this.facetField;
	}

	public int getTermCount() {
		return getTerms().length;
	}

	public String getTerm(int i) {
		return getTerms()[i];
	}

	public int getCount(int i) {
		return getCount()[i];
	}

	@Override
	public String toString() {
		String s = this.getClass().getName() + "@" + this.hashCode();
		String[] terms = this.getTerms();
		int[] count = this.getCount();
		for (int i = 0; i < terms.length; i++)
			if (count[i] > 0)
				s += " " + terms[i] + "(" + count[i] + ")";
		return s;
	}

}
