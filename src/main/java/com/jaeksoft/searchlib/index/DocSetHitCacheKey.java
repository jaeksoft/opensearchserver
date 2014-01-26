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

package com.jaeksoft.searchlib.index;

import java.io.IOException;

import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterListCacheKey;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.scoring.AdvancedScore;

public class DocSetHitCacheKey implements Comparable<DocSetHitCacheKey> {

	private final String query;
	private final Boolean facet;
	private final FilterListCacheKey filterListCacheKey;
	private final String boostQueryCacheKey;
	private final String advancedScoreCacheKey;

	public DocSetHitCacheKey(AbstractSearchRequest searchRequest,
			SchemaField defaultField, PerFieldAnalyzer analyzer)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		Query q = searchRequest.getQuery();
		query = q == null ? "" : q.toString();
		facet = searchRequest.isFacet();
		filterListCacheKey = new FilterListCacheKey(
				searchRequest.getFilterList(), defaultField, analyzer,
				searchRequest);
		boostQueryCacheKey = BoostQuery.getCacheKey(searchRequest
				.getBoostingQueries());
		advancedScoreCacheKey = AdvancedScore.getCacheKey(searchRequest
				.getAdvancedScore());
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
		if ((c = boostQueryCacheKey.compareTo(r.boostQueryCacheKey)) != 0)
			return c;
		if ((c = advancedScoreCacheKey.compareTo(r.advancedScoreCacheKey)) != 0)
			return c;
		return 0;
	}
}
