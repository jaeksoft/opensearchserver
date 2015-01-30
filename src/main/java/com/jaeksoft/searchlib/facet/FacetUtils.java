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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.bitset.BitSetInterface;
import com.opensearchserver.client.v2.search.FacetResult2;

public class FacetUtils {

	final static private FacetResult2 newFacetResult(FacetField facetField,
			String[] terms, long[] counts) {
		FacetResult2 facetResult = new FacetResult2().setFieldName(facetField
				.getName());
		if (terms == null || counts == null)
			return facetResult;
		int i = 0;
		int minCount = facetField.getMinCount();
		LinkedHashMap<String, Long> facetMap = new LinkedHashMap<String, Long>();
		for (long count : counts) {
			String term = terms[i];
			if (term != null && count >= minCount)
				facetMap.put(term.intern(), count);
			i++;
		}
		return facetResult.setTerms(facetMap);
	}

	protected void sum(FacetResult2 facet1, FacetResult2 facet2) {
		if (facet2 == null)
			return;
		for (Map.Entry<String, Long> entry : facet2.terms.entrySet()) {
			String term = entry.getKey();
			Long count1 = facet1.terms.get(term);
			Long count2 = entry.getValue();
			count1 = count1 == null ? count2 : count1 + count2;
			facet1.terms.put(term, count1);
		}
	}

	final static protected FacetResult2 facetMultivalued(ReaderAbstract reader,
			DocIdInterface docIdInterface, FacetField facetField, Timer timer)
			throws IOException, SearchLibException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		long[] countIndex = computeMultivalued(reader, fieldName, stringIndex,
				docIdInterface);
		return newFacetResult(facetField, stringIndex.lookup, countIndex);
	}

	final static protected FacetResult2 facetSingleValue(ReaderAbstract reader,
			DocIdInterface collector, FacetField facetField, Timer timer)
			throws IOException {
		String fieldName = facetField.getName();
		FieldCacheIndex stringIndex = reader.getStringIndex(fieldName);
		long[] countIndex = computeSinglevalued(stringIndex, collector);
		return newFacetResult(facetField, stringIndex.lookup, countIndex);
	}

	final private static long[] computeMultivalued(ReaderAbstract reader,
			String fieldName, FieldCacheIndex stringIndex,
			DocIdInterface docIdInterface) throws IOException,
			SearchLibException {
		long[] countIndex = new long[stringIndex.lookup.length];
		int i = 0;
		if (docIdInterface.getSize() == 0)
			return countIndex;
		BitSetInterface bitset = docIdInterface.getBitSet();
		for (String term : stringIndex.lookup) {
			if (term != null) {
				Term t = new Term(fieldName, term);
				TermDocs termDocs = reader.getTermDocs(t);
				while (termDocs.next())
					if (termDocs.freq() > 0)
						if (bitset.get(termDocs.doc()))
							countIndex[i]++;
				termDocs.close();
			}
			i++;
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
