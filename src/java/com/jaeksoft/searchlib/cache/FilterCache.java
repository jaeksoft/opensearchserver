/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cache;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterCacheKey;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.schema.Field;

public class FilterCache extends LRUCache<FilterCacheKey, FilterHits> {

	private IndexConfig indexConfig;

	public FilterCache(IndexConfig indexConfig) {
		super("Filter cache", indexConfig.getFilterCache());
		this.indexConfig = indexConfig;
	}

	public FilterHits get(ReaderLocal reader, Field defaultField,
			Analyzer analyzer, Filter filter) throws ParseException,
			IOException {
		rwl.w.lock();
		try {
			FilterCacheKey filterCacheKey = null;
			filterCacheKey = new FilterCacheKey(filter, defaultField, analyzer);
			FilterHits filterHits = getAndPromote(filterCacheKey);
			if (filterHits != null)
				return filterHits;
			Query query = filter.getQuery(defaultField, analyzer);
			filterHits = new FilterHits(query, filter.isNegative(), reader);
			put(filterCacheKey, filterHits);
			return filterHits;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void setMaxSize(int newMaxSize) {
		super.setMaxSize(newMaxSize);
		indexConfig.setFilterCache(newMaxSize);
	}
}
