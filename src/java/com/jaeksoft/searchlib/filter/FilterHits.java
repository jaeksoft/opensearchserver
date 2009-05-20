/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.ReaderLocal;

public class FilterHits extends org.apache.lucene.search.Filter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 966120808560552509L;

	protected OpenBitSet docSet;

	protected FilterHits() {
		docSet = null;
	}

	protected void and(FilterHits filterHits) {
		if (docSet == null)
			docSet = (OpenBitSet) filterHits.docSet.clone();
		else
			docSet.and(filterHits.docSet);
	}

	public FilterHits(Query query, ReaderLocal reader) throws IOException,
			ParseException {
		Collector collector = new Collector(reader.maxDoc());
		reader.search(query, null, collector);
		docSet = collector.bitSet;
	}

	private class Collector extends HitCollector {

		private OpenBitSet bitSet;

		private Collector(int size) {
			this.bitSet = new OpenBitSet(size);
		}

		@Override
		public void collect(int docId, float score) {
			bitSet.set(docId);
		}
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		return this.docSet;
	}

}
