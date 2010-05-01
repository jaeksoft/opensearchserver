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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.util.External;

public class Facet implements Externalizable, Iterable<FacetItem>,
		External.Collecter<FacetItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2867011819195319222L;

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

	final static protected Facet facetMultivalued(ResultSingle result,
			FacetField facetField) throws IOException {
		ReaderLocal reader = result.getReader();
		StringIndex stringIndex = reader.getStringIndex(facetField.getName());
		int[] count = new int[stringIndex.lookup.length];
		String fieldName = facetField.getName();
		DocSetHits dsh = result.getDocSetHits();
		int i = 0;
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = new Term(fieldName, term);
				TermDocs termDocs = reader.getTermDocs(t);
				while (termDocs.next())
					if (termDocs.freq() > 0)
						if (dsh.contains(termDocs.doc()))
							count[i]++;
			}
			i++;
		}
		return new Facet(facetField, stringIndex.lookup, count);
	}

	final static protected Facet facetSingleValue(ResultSingle result,
			FacetField facetField) throws IOException {
		StringIndex stringIndex = result.getReader().getStringIndex(
				facetField.getName());
		int[] order = stringIndex.order;
		int[] count = new int[stringIndex.lookup.length];
		for (int id : result.getDocSetHits().getCollectedDocs())
			count[order[id]]++;
		return new Facet(facetField, stringIndex.lookup, count);
	}

	public void addObject(FacetItem facetItem) {
		facetMap.put(facetItem.term, facetItem);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		facetField = (FacetField) External.readObject(in);
		External.readCollection(in, this);

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObject(facetField, out);
		External.writeCollection(facetMap.values(), out);
	}
}
