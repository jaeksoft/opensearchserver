/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.Timer;

public class FilterHits extends Filter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7434283983275758714L;

	protected final Map<IndexReader, OpenBitSet> docSetMap;

	public FilterHits() {
		docSetMap = new HashMap<IndexReader, OpenBitSet>();
	}

	public FilterHits(Query query, boolean negative, ReaderLocal reader,
			Timer timer) throws IOException, ParseException {
		this();
		Timer t = new Timer(timer, "Filter hit: " + query.toString());
		FilterCollector collector = new FilterCollector();
		reader.search(query, null, collector);
		if (negative)
			for (OpenBitSet docSet : docSetMap.values())
				docSet.flip(0, docSet.size());
		t.getDuration();
	}

	final void and(FilterHits sourceFilterHits) {
		for (Map.Entry<IndexReader, OpenBitSet> entry : sourceFilterHits.docSetMap
				.entrySet())
			and(entry.getKey(), entry.getValue());
	}

	private final void and(IndexReader indexReader, OpenBitSet sourceDocSet) {
		OpenBitSet docSet = docSetMap.get(indexReader);
		if (docSet == null)
			docSetMap.put(indexReader, (OpenBitSet) sourceDocSet.clone());
		else
			docSet.and(sourceDocSet);
	}

	private class FilterCollector extends Collector {

		private OpenBitSet currentDocSet;

		@Override
		public final void collect(final int docId) {
			currentDocSet.set(docId);
		}

		@Override
		public final boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public final void setNextReader(final IndexReader reader,
				final int docBase) throws IOException {
			currentDocSet = new OpenBitSet(reader.maxDoc());
			docSetMap.put(reader, currentDocSet);
		}

		@Override
		public final void setScorer(final Scorer scorer) throws IOException {
		}
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		return docSetMap.get(reader);
	}

}
