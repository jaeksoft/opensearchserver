/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Keller / Jaeksoft
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
package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.collector.DocIdCollector;

public class MultiSort extends SorterAbstract {

	private SorterAbstract[] sorters;

	public MultiSort(DocIdCollector collector, SorterAbstract... sorters) {
		super(collector);
		this.sorters = sorters;
	}

	@Override
	final public int compare(int pos1, int pos2) {
		int c;
		for (SorterAbstract sorter : sorters) {
			c = sorter.compare(pos1, pos2);
			if (c != 0)
				return c;
		}
		return 0;
	}

	@Override
	public boolean isScore() {
		for (SorterAbstract sorter : sorters)
			if (sorter.isScore())
				return true;
		return false;
	}

	@Override
	public String toString(int pos) {
		StringBuffer sb = new StringBuffer('[');
		for (SorterAbstract sorter : sorters) {
			sb.append(sorter.toString(pos));
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
}
