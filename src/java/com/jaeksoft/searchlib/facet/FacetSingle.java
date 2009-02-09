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

import com.jaeksoft.searchlib.result.ResultSingle;

public class FacetSingle extends Facet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8151598793687762592L;

	protected FacetSingle(FacetField facetField) {
		super(facetField);
	}

	protected FacetSingle(ResultSingle result, FacetField facetField)
			throws IOException {
		super(facetField);
		StringIndex stringIndex = result.getReader().getStringIndex(
				facetField.getName());
		int[] order = stringIndex.order;
		int[] count = new int[stringIndex.lookup.length];
		for (int id : result.getDocSetHits().getCollectedDocs())
			count[order[id]]++;
		setResult(new FacetCount(stringIndex.lookup, count, facetField
				.getMinCount()));
	}

}
