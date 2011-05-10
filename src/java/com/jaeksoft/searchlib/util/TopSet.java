/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

public class TopSet {
	private Collection<String> source;
	private final TreeMap<Integer, String> treeMap;
	private int max;

	public TopSet(Collection<String> source, int max) {
		this.source = source;
		this.max = max;
		this.treeMap = new TreeMap<Integer, String>();
		generateTreeMap();
	}

	public TreeMap<Integer, String> getTreeMap() {
		return treeMap;
	}

	private void generateTreeMap() {
		Iterator<String> itr = source.iterator();
		int i = 0;
		while (itr.hasNext()) {
			treeMap.put(i, itr.next());
			if (i > max)
				treeMap.pollFirstEntry();
			i++;
		}

	}
}
