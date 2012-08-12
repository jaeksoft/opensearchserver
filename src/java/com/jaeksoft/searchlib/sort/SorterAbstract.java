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

import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;

public abstract class SorterAbstract {

	public abstract void init(DocIdInterface docIdInterface);

	public abstract int compare(int pos1, int pos2);

	final public void quickSort(DocIdInterface docIdInterface) {
		init(docIdInterface);
		new QuickSort(this).sort(docIdInterface);
	}

	public void quickSort(DocIdInterface docIdInterface, Timer timer) {
		Timer t = new Timer(timer, "Sort (quicksort): "
				+ docIdInterface.getNumFound());
		quickSort(docIdInterface);
		t.duration();
	}

	public abstract boolean needScore();

}
