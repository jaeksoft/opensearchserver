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

import java.io.IOException;

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.ResultSearch;

public class FacetSearch extends Facet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8151598793687762592L;
	transient private String[] terms;
	private int[] count;

	public FacetSearch(ResultSearch result, FacetField facetField)
			throws IOException {
		super(facetField);
		StringIndex si = result.getReader()
				.getStringIndex(facetField.getName());
		this.terms = si.lookup;
		int[] order = si.order;
		this.count = new int[this.terms.length];
		for (int id : result.getUnsortedDocFound())
			this.count[order[id]]++;
	}

	public void setReader(ReaderLocal reader) throws IOException {
		StringIndex si = reader.getStringIndex(facetField.getName());
		this.terms = si.lookup;
	}

	@Override
	public String[] getTerms() {
		return this.terms;
	}

	@Override
	public int[] getCount() {
		return this.count;
	}
}
