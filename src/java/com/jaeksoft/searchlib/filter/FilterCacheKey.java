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

package com.jaeksoft.searchlib.filter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.schema.Field;

public class FilterCacheKey implements CacheKeyInterface<FilterCacheKey> {

	private String query;

	private boolean isNegative;

	public FilterCacheKey(Filter filter, Field defaultField, Analyzer analyzer)
			throws ParseException {
		query = filter.getQuery(defaultField, analyzer).toString();
		isNegative = filter.isNegative();
	}

	@Override
	public int compareTo(FilterCacheKey o) {
		if (isNegative != o.isNegative)
			return isNegative ? -1 : 1;
		return query.compareTo(o.query);
	}

	@Override
	public String toString() {
		return hashCode() + ' ' + query;
	}
}
