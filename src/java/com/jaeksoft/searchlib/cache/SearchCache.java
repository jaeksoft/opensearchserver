/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHitCacheKey;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.Schema;

public class SearchCache extends LRUCache<DocSetHitCacheKey, DocSetHits> {

	private IndexConfig indexConfig;

	public SearchCache(IndexConfig indexConfig) {
		super("Search cache", indexConfig.getSearchCache());
		this.indexConfig = indexConfig;
	}

	public DocSetHits get(ReaderLocal reader, SearchRequest searchRequest,
			Schema schema, Field defaultField, Analyzer analyzer)
			throws ParseException, SyntaxError, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		rwl.w.lock();
		try {
			DocSetHitCacheKey key = new DocSetHitCacheKey(searchRequest,
					defaultField, analyzer);
			DocSetHits dsh = getAndPromote(key);
			if (dsh != null)
				return dsh;
			dsh = reader.newDocSetHits(searchRequest, schema, defaultField,
					analyzer);
			put(key, dsh);
			return dsh;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void setMaxSize(int newMaxSize) {
		super.setMaxSize(newMaxSize);
		indexConfig.setSearchCache(newMaxSize);
	}
}
