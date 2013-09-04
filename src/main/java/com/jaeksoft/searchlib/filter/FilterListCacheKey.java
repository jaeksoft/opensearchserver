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

import java.util.Iterator;
import java.util.TreeSet;

import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.SchemaField;

public class FilterListCacheKey implements
		CacheKeyInterface<FilterListCacheKey> {

	private TreeSet<FilterCacheKey> filterCacheKeySet;

	public FilterListCacheKey(FilterList filterList, SchemaField defaultField,
			PerFieldAnalyzer analyzer) throws ParseException {
		filterCacheKeySet = new TreeSet<FilterCacheKey>();
		for (FilterAbstract<?> filter : filterList)
			filterCacheKeySet.add(new FilterCacheKey(filter, defaultField,
					analyzer));
	}

	@Override
	public int compareTo(FilterListCacheKey o) {
		int i1 = filterCacheKeySet.size();
		int i2 = o.filterCacheKeySet.size();
		if (i1 < i2)
			return -1;
		else if (i1 > i2)
			return 1;
		Iterator<FilterCacheKey> it = o.filterCacheKeySet.iterator();
		for (FilterCacheKey filterCacheKey : filterCacheKeySet) {
			int c = filterCacheKey.compareTo(it.next());
			if (c != 0)
				return c;
		}
		return 0;
	}

}
