/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Keller / Jaeksoft
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

import com.jaeksoft.searchlib.result.collector.CollectorInterface;

public class MultiSort extends SorterAbstract {

	final private SorterAbstract[] sorters;

	public MultiSort(final CollectorInterface collector,
			final SorterAbstract... sorters) {
		super(collector);
		this.sorters = sorters;
	}

	@Override
	final public int compare(final int pos1, final int pos2) {
		int c;
		for (SorterAbstract sorter : sorters) {
			c = sorter.compare(pos1, pos2);
			if (c != 0)
				return c;
		}
		return 0;
	}

	@Override
	final public boolean isScore() {
		for (SorterAbstract sorter : sorters)
			if (sorter.isScore())
				return true;
		return false;
	}

	@Override
	final public boolean isDistance() {
		for (SorterAbstract sorter : sorters)
			if (sorter.isDistance())
				return true;
		return false;
	}

	@Override
	final public String toString(final int pos) {
		StringBuilder sb = new StringBuilder('[');
		for (SorterAbstract sorter : sorters) {
			sb.append(sorter.toString(pos));
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
}
