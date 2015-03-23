/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.bitset.BitSetInterface;

public class FacetUtils {

	final static private Map<String, Long> newFacetResult(
			FacetField facetField, String[] terms, long[] counts) {
		if (terms == null || counts == null)
			return Collections.<String, Long> emptyMap();
		int i = 0;
		int minCount = facetField.getMinCount();
		LinkedHashMap<String, Long> facetMap = new LinkedHashMap<String, Long>();
		for (long count : counts) {
			String term = terms[i];
			if (term != null && count >= minCount)
				facetMap.put(term, count);
			i++;
		}
		return facetMap;
	}

	final static private Map<String, Long> newFacetResult(
			FacetField facetField, Map<String, Long> facets) {
		if (facets == null || facets.isEmpty())
			return Collections.<String, Long> emptyMap();
		int minCount = facetField.getMinCount();
		LinkedHashMap<String, Long> facetMap = new LinkedHashMap<String, Long>();
		for (Map.Entry<String, Long> entry : facets.entrySet())
			if (entry.getValue() >= minCount)
				facetMap.put(entry.getKey(), entry.getValue());
		return facetMap;
	}

	protected void sum(Map<String, Long> facet1, Map<String, Long> facet2) {
		if (facet2 == null)
			return;
		for (Map.Entry<String, Long> entry : facet2.entrySet()) {
			String term = entry.getKey();
			Long count1 = facet1.get(term);
			Long count2 = entry.getValue();
			count1 = count1 == null ? count2 : count1 + count2;
			facet1.put(term, count1);
		}
	}

	final static protected Map<String, Long> facetMultivalued(
			ReaderAbstract reader, SchemaField schemaField,
			DocIdInterface docIdInterface, FacetField facetField, Timer timer)
			throws IOException, SearchLibException {
		String fieldName = facetField.getName();
		if (schemaField.getTermVector() == TermVector.NO) {
			FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
			long[] countIndex = computeMultivaluedTD(reader, fieldName,
					stringIndex, docIdInterface);
			return newFacetResult(facetField, stringIndex.lookup, countIndex);
		} else {
			Map<String, Long> facets = computeMultivaluedTVF(reader, fieldName,
					docIdInterface);
			return newFacetResult(facetField, facets);
		}

	}

	final static protected Map<String, Long> facetSingleValue(
			ReaderAbstract reader, DocIdInterface collector,
			FacetField facetField, Timer timer) throws IOException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		long[] countIndex = computeSinglevalued(stringIndex, collector);
		return newFacetResult(facetField, stringIndex.lookup, countIndex);
	}

	final private static Map<String, Long> computeMultivaluedTVF(
			ReaderAbstract reader, String fieldName,
			DocIdInterface docIdInterface) throws IOException,
			SearchLibException {
		Map<String, Long> termMap = new HashMap<String, Long>();
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
					Long count = termMap.get(term);
					if (count == null)
						count = 1L;
					else
						count++;
					termMap.put(term, count);
				}
			}
		}
		return termMap;
	}

	final private static long[] computeMultivaluedTD(ReaderAbstract reader,
			String fieldName, FieldCacheIndex stringIndex,
			DocIdInterface docIdInterface) throws IOException,
			SearchLibException {
		long[] countIndex = new long[stringIndex.lookup.length];
		int indexPos = 0;
		if (docIdInterface.getSize() == 0)
			return countIndex;
		int[] docs = new int[100];
		int[] freqs = new int[100];
		BitSetInterface bitset = docIdInterface.getBitSet();
		Term oTerm = new Term(fieldName);
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = oTerm.createTerm(term);
				TermDocs termDocs = reader.getTermDocs(t);
				int l;
				while ((l = termDocs.read(docs, freqs)) > 0)
					for (int i = 0; i < l; i++)
						if (freqs[i] > 0)
							if (bitset.get(docs[i]))
								countIndex[indexPos]++;
				termDocs.close();
			}
			indexPos++;
		}
		return countIndex;
	}

	final private static long[] computeSinglevalued(
			FieldCacheIndex stringIndex, DocIdInterface collector)
			throws IOException {
		long[] countArray = new long[stringIndex.lookup.length];
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
