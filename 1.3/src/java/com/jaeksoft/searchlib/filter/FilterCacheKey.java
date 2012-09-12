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

package com.jaeksoft.searchlib.filter;

import org.apache.lucene.analysis.Analyzer;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.SchemaField;

public class FilterCacheKey implements CacheKeyInterface<FilterCacheKey> {

	private String key;

	private boolean isNegative;

	public FilterCacheKey(FilterAbstract<?> filter, SchemaField defaultField,
			Analyzer analyzer) throws ParseException {
		key = filter.getCacheKey(defaultField, analyzer);
		isNegative = filter.isNegative();
	}

	@Override
	public int compareTo(FilterCacheKey o) {
		if (isNegative != o.isNegative)
			return isNegative ? -1 : 1;
		return key.compareTo(o.key);
	}

	@Override
	public String toString() {
		return hashCode() + ' ' + key;
	}
}
