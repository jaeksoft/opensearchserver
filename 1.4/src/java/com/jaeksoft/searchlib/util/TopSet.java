/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.util.Comparator;
import java.util.TreeSet;

public class TopSet<T> {

	private final TreeSet<T> treeSet;

	private int max;

	/**
	 * Build a sorted set from an array. The size of the Set is limited by the
	 * max parameter.
	 * 
	 * @param array
	 * @param comparator
	 * @param max
	 */
	public TopSet(T[] array, Comparator<T> comparator, int max) {
		this.max = max;
		this.treeSet = new TreeSet<T>(comparator);
		generateTreeMap(array);
	}

	private void generateTreeMap(T[] array) {
		int i = 0;
		for (T item : array) {
			treeSet.add(item);
			if (i++ > max)
				treeSet.pollFirst();
		}
	}

	public TreeSet<T> getTreeMap() {
		return treeSet;
	}

}
