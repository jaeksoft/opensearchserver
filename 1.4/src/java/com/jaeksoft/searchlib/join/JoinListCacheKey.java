/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.join;

import java.util.Iterator;
import java.util.TreeSet;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.query.ParseException;

public class JoinListCacheKey implements CacheKeyInterface<JoinListCacheKey> {

	private TreeSet<JoinItem> joinCacheKeySet;

	public JoinListCacheKey(JoinList joinList) throws ParseException {
		joinCacheKeySet = new TreeSet<JoinItem>();
		for (JoinItem joinItem : joinList)
			joinCacheKeySet.add(joinItem);
	}

	@Override
	public int compareTo(JoinListCacheKey o) {
		int i1 = joinCacheKeySet.size();
		int i2 = o.joinCacheKeySet.size();
		if (i1 < i2)
			return -1;
		else if (i1 > i2)
			return 1;
		Iterator<JoinItem> it = o.joinCacheKeySet.iterator();
		for (JoinItem joinItem : joinCacheKeySet) {
			int c = joinItem.compareTo(it.next());
			if (c != 0)
				return c;
		}
		return 0;
	}

}
