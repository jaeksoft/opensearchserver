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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.FilterListCacheKey;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.scoring.AdvancedScore;

public class DocSetHitCacheKey implements Comparable<DocSetHitCacheKey> {

	private final String query;
	private final Boolean isScoreRequired;
	private final Boolean isDistanceRequired;
	private final Boolean isDocIdRequired;
	private final FilterListCacheKey filterListCacheKey;
	private final String boostQueryCacheKey;
	private final String advancedScoreCacheKey;

	public DocSetHitCacheKey(SchemaField defaultField, Analyzer analyzer,
			Query query, boolean isScoreRequired, boolean isDistanceRequired,
			GeoParameters geoParameters, FilterList filterList,
			AdvancedScore advancedScore, boolean isDocIdRequired,
			BoostQuery[] boostQueries, AbstractSearchRequest request)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		this.query = query == null ? "" : query.toString();
		this.isScoreRequired = isScoreRequired;
		this.isDistanceRequired = isDistanceRequired;
		this.isDocIdRequired = isDocIdRequired;
		filterListCacheKey = new FilterListCacheKey(filterList, defaultField,
				analyzer, request);
		boostQueryCacheKey = BoostQuery.getCacheKey(boostQueries);
		advancedScoreCacheKey = AdvancedScore.getCacheKey(advancedScore);
	}

	public DocSetHitCacheKey(AbstractSearchRequest searchRequest,
			SchemaField defaultField, PerFieldAnalyzer analyzer)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		this(defaultField, analyzer, searchRequest.getQuery(), searchRequest
				.isScoreRequired(), searchRequest.isDistanceRequired(),
				searchRequest.getGeoParameters(),
				searchRequest.getFilterList(),
				searchRequest.getAdvancedScore(), searchRequest
						.isDocIdRequired(), searchRequest.getBoostingQueries(),
				searchRequest);
	}

	@Override
	public int compareTo(DocSetHitCacheKey r) {
		int c;
		if ((c = query.compareTo(r.query)) != 0)
			return c;
		if ((c = isScoreRequired.compareTo(r.isScoreRequired)) != 0)
			return c;
		if ((c = isDistanceRequired.compareTo(r.isDistanceRequired)) != 0)
			return c;
		if ((c = isDocIdRequired.compareTo(r.isDocIdRequired)) != 0)
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
