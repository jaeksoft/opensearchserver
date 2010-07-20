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

package com.jaeksoft.searchlib.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.filter.FilterListCacheKey;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;

public class DocSetHitCacheKey implements CacheKeyInterface<DocSetHitCacheKey> {

	private String query;
	private Boolean facet;
	private String sortListCacheKey;
	private FilterListCacheKey filterListCacheKey;

	public DocSetHitCacheKey(SearchRequest searchRequest, Field defaultField,
			Analyzer analyzer) throws ParseException, SyntaxError,
			SearchLibException {
		query = searchRequest.getQuery().toString();
		facet = searchRequest.isFacet();
		sortListCacheKey = searchRequest.getSortList().getFieldList()
				.getCacheKey();
		filterListCacheKey = new FilterListCacheKey(
				searchRequest.getFilterList(), defaultField, analyzer);
	}

	@Override
	public int compareTo(DocSetHitCacheKey r) {
		int c;
		if ((c = query.compareTo(r.query)) != 0)
			return c;
		if ((c = facet.compareTo(r.facet)) != 0)
			return c;
		if ((c = filterListCacheKey.compareTo(r.filterListCacheKey)) != 0)
			return c;
		if ((c = sortListCacheKey.compareTo(r.sortListCacheKey)) != 0)
			return c;
		return 0;
	}
}
