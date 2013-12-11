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

package com.jaeksoft.searchlib.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.TermFreqVector;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContentCacheKey;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.Timer;

public class TermVectorCache extends LRUCache<FieldContentCacheKey, String[]> {

	private IndexConfig indexConfig;

	public TermVectorCache(IndexConfig indexConfig) {
		super("TermVector cache", indexConfig.getTermVectorCache());
		this.indexConfig = indexConfig;
	}

	final public String[] get(final ReaderLocal reader, final int docId,
			final String field, final Timer timer) throws IOException,
			ParseException, SyntaxError {
		FieldContentCacheKey key = new FieldContentCacheKey(field, docId);
		String[] terms = getAndPromote(key);
		if (terms != null)
			return terms;
		put(key, terms);
		return terms;
	}

	final public void put(final ReaderLocal reader, final int[] docIds,
			final String field, Collection<String[]> termVectors)
			throws IOException {
		if (docIds == null)
			return;
		List<String[]> termVectorList = new ArrayList<String[]>(docIds.length);
		Map<FieldContentCacheKey, Integer> keysMap = new LinkedHashMap<FieldContentCacheKey, Integer>();
		int i = 0;
		for (int docId : docIds) {
			FieldContentCacheKey key = new FieldContentCacheKey(field, docId);
			String[] terms = getAndPromote(key);
			termVectorList.add(terms);
			if (terms == null)
				keysMap.put(key, i);
			i++;
		}
		if (keysMap.size() > 0) {
			int[] getDocIds = new int[keysMap.size()];
			i = 0;
			for (Map.Entry<FieldContentCacheKey, Integer> entry : keysMap
					.entrySet())
				getDocIds[i++] = entry.getKey().docId;
			List<TermFreqVector> termFreqVectors = new ArrayList<TermFreqVector>(
					getDocIds.length);
			reader.putTermFreqVectors(getDocIds, field, termFreqVectors);
			i = 0;
			for (Map.Entry<FieldContentCacheKey, Integer> entry : keysMap
					.entrySet()) {
				final String[] terms = termFreqVectors.get(i++).getTerms();
				termVectorList.set(entry.getValue(), terms);
				put(entry.getKey(), terms);
			}
		}
		for (String[] terms : termVectorList)
			termVectors.add(terms);
	}

	@Override
	public void setMaxSize(int newMaxSize) {
		super.setMaxSize(newMaxSize);
		indexConfig.setTermVectorCache(newMaxSize);
	}
}
