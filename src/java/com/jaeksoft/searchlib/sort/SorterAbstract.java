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

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.IntComparator;

import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;

public abstract class SorterAbstract implements IntComparator {

	private DocIdInterface collector;

	protected SorterAbstract(DocIdInterface collector) {
		this.collector = collector;
	}

	public abstract String toString(int pos);

	@Override
	final public int compare(Integer pos1, Integer pos2) {
		return compare((int) pos1, (int) pos2);
	}

	public void quickSort(Timer timer) {
		Timer t = new Timer(timer, "Sort (quicksort): " + collector.getSize());
		Arrays.quickSort(0, collector.getSize(), this, collector);
		t.duration();
		// check(timer);
	}

	public void check(Timer timer) {
		int l = collector.getSize();
		if (l == 0)
			return;
		Timer t = new Timer(timer, "Check sort (quicksort) " + l);
		int last = 0;
		int err = 0;
		for (int i = 1; i < l; i++) {
			if (compare(last, i) > 0) {
				System.out.println(last + ": " + toString(last) + " - " + i
						+ ": " + toString(i));
				err++;
			}
			last = i;
		}
		if (err > 0)
			System.out.println("SORT ERROR: " + err + " / " + l);
		t.duration();
	}

	public abstract boolean isScore();

}
