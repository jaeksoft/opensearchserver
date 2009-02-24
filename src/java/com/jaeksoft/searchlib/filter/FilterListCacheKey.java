/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.filter;

import java.util.Iterator;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.schema.Field;

public class FilterListCacheKey implements
		CacheKeyInterface<FilterListCacheKey> {

	private TreeSet<FilterCacheKey> filterCacheKeySet;

	public FilterListCacheKey(FilterList filterList, Field defaultField,
			Analyzer analyzer) throws ParseException {
		filterCacheKeySet = new TreeSet<FilterCacheKey>();
		for (Filter filter : filterList)
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
