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

package com.jaeksoft.searchlib.sort;

import java.io.IOException;

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.util.Timer;

public class SortListSorter extends SorterAbstract {

	final private SorterAbstract[] sorterList;

	protected SortListSorter(final SortFieldList sortFieldList,
			final CollectorInterface collector, final ReaderAbstract reader)
			throws IOException {
		super(collector);
		sorterList = new SorterAbstract[sortFieldList.size()];
		int i = 0;
		for (SortField sortField : sortFieldList)
			sorterList[i++] = sortField.getSorter(collector, reader);
	}

	@Override
	final public int compare(final int pos1, final int pos2) {
		for (SorterAbstract sorter : sorterList) {
			int c = sorter.compare(pos1, pos2);
			if (c != 0)
				return c;
		}
		return 0;
	}

	@Override
	final public void quickSort(final Timer timer) {
		if (sorterList.length == 1)
			sorterList[0].quickSort(timer);
		else
			super.quickSort(timer);
	}

	@Override
	final public boolean isScore() {
		for (SorterAbstract sorter : sorterList)
			if (sorter.isScore())
				return true;
		return false;
	}

	@Override
	final public boolean isDistance() {
		for (SorterAbstract sorter : sorterList)
			if (sorter.isDistance())
				return true;
		return false;
	}

	@Override
	final public String toString(final int pos) {
		StringBuilder sb = new StringBuilder('[');
		for (SorterAbstract sorter : sorterList) {
			sb.append(sorter.toString(pos));
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
}
