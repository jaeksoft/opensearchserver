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

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;

public class SortListSorter extends SorterAbstract {

	private SorterAbstract[] sorterList;

	protected SortListSorter(SortFieldList sortFieldList,
			DocIdInterface collector, ReaderLocal reader) throws IOException {
		super(collector);
		sorterList = new SorterAbstract[sortFieldList.size()];
		int i = 0;
		for (SortField sortField : sortFieldList)
			sorterList[i++] = sortField.getSorter(collector, reader);
	}

	@Override
	final public int compare(int pos1, int pos2) {
		for (SorterAbstract sorter : sorterList) {
			int c = sorter.compare(pos1, pos2);
			if (c != 0)
				return c;
		}
		return 0;
	}

	@Override
	final public void quickSort(Timer timer) {
		if (sorterList.length == 1)
			sorterList[0].quickSort(timer);
		else
			super.quickSort(timer);
	}

	@Override
	public boolean isScore() {
		for (SorterAbstract sorter : sorterList)
			if (sorter.isScore())
				return true;
		return false;
	}

	@Override
	public String toString(int pos) {
		StringBuffer sb = new StringBuffer('[');
		for (SorterAbstract sorter : sorterList) {
			sb.append(sorter.toString(pos));
			sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
}
