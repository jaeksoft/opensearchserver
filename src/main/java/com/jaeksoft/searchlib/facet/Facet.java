/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.bitset.BitSetInterface;

public class Facet implements Iterable<FacetItem>,
		External.Collecter<FacetItem> {

	protected FacetField facetField;
	private Map<String, FacetItem> facetMap;
	protected transient List<FacetItem> list = null;

	public Facet() {
		list = null;
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

	private Facet(FacetField facetField, Map<String, Integer> treeMap) {
		this(facetField);
		int minCount = facetField.getMinCount();
		for (Map.Entry<String, Integer> entry : treeMap.entrySet())
			if (entry.getValue() >= minCount)
				facetMap.put(entry.getKey(), new FacetItem(entry.getKey(),
						entry.getValue()));
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

	public List<FacetItem> getList() {
		synchronized (this) {
			if (list != null)
				return list;
			list = new ArrayList<FacetItem>(facetMap.values());
			return list;
		}
	}

	public Map<String, FacetItem> getMap() {
		synchronized (this) {
			return facetMap;
		}
	}

	private FacetItem get(int i) {
		return getList().get(i);
	}

	@Override
	public Iterator<FacetItem> iterator() {
		return facetMap.values().iterator();
	}

	public int getTermCount() {
		return getList().size();
	}

	public String getTerm(int i) {
		return get(i).term;
	}

	public int getCount(int i) {
		return get(i).count;
	}

	final static protected Facet facetMultivalued(ReaderAbstract reader,
			DocIdInterface docIdInterface, FacetField facetField, Timer timer)
			throws IOException, SearchLibException {
		String fieldName = facetField.getName();
		Map<String, Integer> facetMap = computeMultivalued(reader, fieldName,
				docIdInterface);
		return new Facet(facetField, facetMap);
	}

	final static protected Facet facetSingleValue(ReaderAbstract reader,
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

	final private static int[] computeMultivaluedOld(ReaderAbstract reader,
			String fieldName, FieldCacheIndex stringIndex,
			DocIdInterface docIdInterface) throws IOException,
			SearchLibException {
		int[] countIndex = new int[stringIndex.lookup.length];
		int indexPos = 0;
		if (docIdInterface.getSize() == 0)
			return countIndex;
		int[] docs = new int[100];
		int[] freqs = new int[100];
		BitSetInterface bitset = docIdInterface.getBitSet();
		Term oTerm = new Term(fieldName);
		int checkCount = 0;
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = oTerm.createTerm(term);
				TermDocs termDocs = reader.getTermDocs(t);
				int l;
				while ((l = termDocs.read(docs, freqs)) > 0)
					for (int i = 0; i < l; i++)
						if (freqs[i] > 0)
							if (bitset.get(docs[i])) {
								countIndex[indexPos]++;
								checkCount++;
							}
				termDocs.close();
			}
			indexPos++;
		}
		System.out.println("CheckCount: " + checkCount);
		return countIndex;
	}

	final private static Map<String, Integer> computeMultivalued(
			ReaderAbstract reader, String fieldName,
			DocIdInterface docIdInterface) throws IOException,
			SearchLibException {
		Map<String, Integer> termMap = new TreeMap<String, Integer>();
		if (docIdInterface.getSize() == 0)
			return termMap;
		for (int docId : docIdInterface.getIds()) {
			TermFreqVector tfv = reader.getTermFreqVector(docId, fieldName);
			if (tfv == null)
				continue;
			String[] terms = tfv.getTerms();
			int[] freqs = tfv.getTermFrequencies();
			if (terms == null || freqs == null)
				continue;
			int i = 0;
			for (String term : terms) {
				if (freqs[i++] > 0) {
					Integer count = termMap.get(term);
					if (count == null)
						count = 1;
					else
						count++;
					termMap.put(term, count);
				}
			}
		}
		return termMap;
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
