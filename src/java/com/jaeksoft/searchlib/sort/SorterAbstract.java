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

import java.util.Arrays;
import java.util.Comparator;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.util.Timer;

public abstract class SorterAbstract implements Comparator<ResultScoreDoc> {

	@Override
	public abstract int compare(ResultScoreDoc doc1, ResultScoreDoc doc2);

	final public void quickSort(ResultScoreDoc[] docs) {
		new QuickSort(this).sort(docs);
	}

	public void quickSort(ResultScoreDoc[] docs, Timer timer) {
		Timer t = new Timer(timer, "Sort (quicksort): " + docs.length);
		quickSort(docs);
		t.duration();
	}

	final public void arraySort(ResultScoreDoc[] docs) {
		Arrays.<ResultScoreDoc> sort(docs, this);
	}

	public void arraySort(ResultScoreDoc[] docs, Timer timer) {
		Timer t = new Timer(timer, "Sort (arraySort): " + docs.length);
		arraySort(docs);
		t.duration();
	}
}
