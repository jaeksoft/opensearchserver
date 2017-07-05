/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.facet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetCounter.FacetSorter;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.util.Timer;
import it.unimi.dsi.fastutil.Arrays;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Facet implements Iterable<Map.Entry<String, FacetCounter>> {

	protected FacetField facetField;
	private Map<String, FacetCounter> facetMap;
	protected transient List<Map.Entry<String, FacetCounter>> list = null;

	public Facet() {
		list = null;
		facetMap = new LinkedHashMap<>();
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
			if (term != null && count >= minCount)
				facetMap.put(term, new FacetCounter(count));
			i++;
		}
	}

	private Facet(FacetField facetField, Map<String, FacetCounter> facetMap) {
		this(facetField);
		int minCount = facetField.getMinCount();
		for (Map.Entry<String, FacetCounter> entry : facetMap.entrySet()) {
			String term = entry.getKey();
			FacetCounter counter = entry.getValue();
			if (term != null && counter.count >= minCount)
				this.facetMap.put(term, counter);
		}
	}

	public FacetField getFacetField() {
		return this.facetField;
	}

	protected void sum(Facet facet) {
		if (facet == null)
			return;
		for (Map.Entry<String, FacetCounter> entry : facet) {
			String term = entry.getKey();
			if (term == null)
				continue;
			FacetCounter value = entry.getValue();
			FacetCounter count = facetMap.get(term);
			if (count == null)
				facetMap.put(term, new FacetCounter(value));
			else
				count.add(value);
		}
	}

	public List<Map.Entry<String, FacetCounter>> getList() {
		synchronized (this) {
			if (list != null)
				return list;
			list = new ArrayList<>(facetMap.entrySet());
			list = limitOrderBy(facetField, list);
			return list;
		}
	}

	private Map.Entry<String, FacetCounter> get(int i) {
		return getList().get(i);
	}

	@Override
	public Iterator<Map.Entry<String, FacetCounter>> iterator() {
		return getList().iterator();
	}

	public int getTermCount() {
		return getList().size();
	}

	public String getTerm(int i) {
		return get(i).getKey();
	}

	public long getCount(int i) {
		return get(i).getValue().count;
	}

	final static protected Facet facetMultivalued(ReaderAbstract reader, SchemaField schemaField,
			DocIdInterface docIdInterface, FacetField facetField, Timer timer) throws IOException, SearchLibException {
		String fieldName = facetField.getName();
		if (schemaField.getTermVector() == TermVector.NO) {
			FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
			int[] countIndex = computeMultivaluedTD(reader, fieldName, stringIndex, docIdInterface);
			return new Facet(facetField, stringIndex.lookup, countIndex);
		} else {
			Map<String, FacetCounter> facetMap = computeMultivaluedTFV(reader, fieldName, docIdInterface);
			return new Facet(facetField, facetMap);
		}
	}

	final static protected Facet facetSingleValue(ReaderAbstract reader, DocIdInterface collector,
			FacetField facetField, Timer timer) throws IOException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		int[] countIndex = computeSinglevalued(stringIndex, collector);
		return new Facet(facetField, stringIndex.lookup, countIndex);
	}

	final private static int[] computeMultivaluedTD(ReaderAbstract reader, String fieldName,
			FieldCacheIndex stringIndex, DocIdInterface docIdInterface) throws IOException, SearchLibException {
		final int[] countIndex = new int[stringIndex.lookup.length];
		int indexPos = 0;
		if (docIdInterface.getSize() == 0)
			return countIndex;
		final int[] docs = new int[100];
		final int[] freqs = new int[100];
		final RoaringBitmap bitset = docIdInterface.getBitSet();
		Term oTerm = new Term(fieldName);
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = oTerm.createTerm(term);
				TermDocs termDocs = reader.getTermDocs(t);
				int l;
				while ((l = termDocs.read(docs, freqs)) > 0)
					for (int i = 0; i < l; i++)
						if (freqs[i] > 0)
							if (bitset.contains(docs[i]))
								countIndex[indexPos]++;
				termDocs.close();
			}
			indexPos++;
		}
		return countIndex;
	}

	private static Map<String, FacetCounter> computeMultivaluedTFV(ReaderAbstract reader, String fieldName,
			DocIdInterface docIdInterface) throws IOException, SearchLibException {
		final Map<String, FacetCounter> termMap = new LinkedHashMap<>();
		if (docIdInterface.getSize() == 0)
			return termMap;
		for (int docId : docIdInterface.getIds()) {
			final TermFreqVector tfv = reader.getTermFreqVector(docId, fieldName);
			if (tfv == null)
				continue;
			final String[] terms = tfv.getTerms();
			final int[] freqs = tfv.getTermFrequencies();
			if (terms == null || freqs == null)
				continue;
			int i = 0;
			for (String term : terms) {
				if (freqs[i++] > 0) {
					final FacetCounter facetItem = termMap.get(term);
					if (facetItem == null)
						termMap.put(term, new FacetCounter(1));
					else
						facetItem.increment();
				}
			}
		}
		return termMap;
	}

	private static int[] computeSinglevalued(FieldCacheIndex stringIndex, DocIdInterface collector) throws IOException {
		final int[] countArray = new int[stringIndex.lookup.length];
		final int[] order = stringIndex.order;
		int i = collector.getSize();
		for (int id : collector.getIds()) {
			if (i == 0)
				break;
			countArray[order[id]]++;
			i--;
		}
		return countArray;
	}

	private static List<Map.Entry<String, FacetCounter>> limitOrderBy(FacetField facetField,
			List<Map.Entry<String, FacetCounter>> list) {
		final FacetSorter facetSorter = FacetSorter.getSorter(list, facetField.getOrderBy());
		if (facetSorter == null)
			return list;
		Arrays.quickSort(0, list.size(), facetSorter, facetSorter);
		final Integer limit = facetField.getLimit();
		if (limit == null)
			return list;
		if (list.size() <= limit)
			return list;
		return list.subList(0, limit);

	}

}
