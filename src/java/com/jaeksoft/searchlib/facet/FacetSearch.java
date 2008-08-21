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

	transient protected StringIndex stringIndex;
	protected int[] count;

	protected FacetSearch(FacetField facetField) {
		super(facetField);
		stringIndex = null;
		count = null;
	}

	protected FacetSearch(ResultSearch result, FacetField facetField)
			throws IOException {
		super(facetField);
		setReader(result.getReader());
		int[] order = stringIndex.order;
		this.count = new int[stringIndex.lookup.length];
		for (int id : result.getUnsortedDocFound())
			this.count[order[id]]++;
	}

	public void setReader(ReaderLocal reader) throws IOException {
		stringIndex = reader.getStringIndex(facetField.getName());
	}

	@Override
	public String[] getTerms() {
		return stringIndex.lookup;
	}

	@Override
	public int[] getCount() {
		return count;
	}
}
