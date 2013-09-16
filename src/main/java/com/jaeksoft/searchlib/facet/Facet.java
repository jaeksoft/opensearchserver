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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.Timer;

public class Facet implements Iterable<FacetItem>,
		External.Collecter<FacetItem> {

	protected FacetField facetField;
	private Map<String, FacetItem> facetMap;
	protected transient FacetItem[] array = null;

	public Facet() {
		array = null;
		facetMap = new TreeMap<String, FacetItem>();
	}

	public Facet(FacetField facetField) {
		this();
		this.facetField = facetField;
	}

	private Facet(FacetField facetField, String[] terms, int[] counts) {
		this(facetField);
		int i = 0;
		int minCount = facetField.getMinCount();
		for (int count : counts) {
			String term = terms[i];
			if (term != null && count >= minCount) {
				FacetItem facetItem = new FacetItem(term, count);
				facetMap.put(term, facetItem);
			}
			i++;
		}
	}

	public FacetField getFacetField() {
		return this.facetField;
	}

	protected void sum(Facet facet) {
		if (facet == null)
			return;
		for (FacetItem facetItem : facet) {
			if (facetItem.term == null)
				continue;
			FacetItem currentFacetItem = facetMap.get(facetItem.term);
			if (currentFacetItem != null)
				currentFacetItem.count += facetItem.count;
			else
				facetMap.put(facetItem.term, facetItem);
		}
	}

	public FacetItem[] getArray() {
		synchronized (this) {
			if (array != null)
				return array;
			array = new FacetItem[facetMap.size()];
			facetMap.values().toArray(array);
			return array;
		}
	}

	public Map<String, FacetItem> getMap() {
		synchronized (this) {
			return facetMap;
		}
	}

	private FacetItem get(int i) {
		return (FacetItem) getArray()[i];
	}

	@Override
	public Iterator<FacetItem> iterator() {
		return facetMap.values().iterator();
	}

	public int getTermCount() {
		return getArray().length;
	}

	public String getTerm(int i) {
		return get(i).term;
	}

	public int getCount(int i) {
		return get(i).count;
	}

	final static protected Facet facetMultivalued(ReaderLocal reader,
			DocIdInterface collector, FacetField facetField, Timer timer)
			throws IOException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		int[] countIndex = computeMultivalued(reader, fieldName, stringIndex,
				collector.getBitSet());
		return new Facet(facetField, stringIndex.lookup, countIndex);
	}

	final static protected Facet facetSingleValue(ReaderLocal reader,
			DocIdInterface collector, FacetField facetField, Timer timer)
			throws IOException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		int[] countIndex = computeSinglevalued(stringIndex, collector);
		return new Facet(facetField, stringIndex.lookup, countIndex);
	}

	@Override
	public void addObject(FacetItem facetItem) {
		facetMap.put(facetItem.term, facetItem);
	}

	final private static int[] computeMultivalued(ReaderLocal reader,
			String fieldName, FieldCacheIndex stringIndex, OpenBitSet bitset)
			throws IOException {
		int[] countIndex = new int[stringIndex.lookup.length];
		int i = 0;
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = new Term(fieldName, term);
				TermDocs termDocs = reader.getTermDocs(t);
				while (termDocs.next())
					if (termDocs.freq() > 0)
						if (bitset.fastGet(termDocs.doc()))
							countIndex[i]++;
				termDocs.close();
			}
			i++;
		}
		return countIndex;
	}

	final private static int[] computeSinglevalued(FieldCacheIndex stringIndex,
			DocIdInterface collector) throws IOException {
		int[] countArray = new int[stringIndex.lookup.length];
		int[] order = stringIndex.order;
		int i = collector.getSize();
		for (int id : collector.getIds()) {
			if (i == 0)
				break;
			countArray[order[id]]++;
			i--;
		}
		return countArray;
	}

}
