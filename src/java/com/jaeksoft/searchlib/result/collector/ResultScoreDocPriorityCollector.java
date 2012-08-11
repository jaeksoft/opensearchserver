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

package com.jaeksoft.searchlib.result.collector;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.sort.priorityQueue.SetPriorityQueue;
import com.jaeksoft.searchlib.util.Timer;

public class ResultScoreDocPriorityCollector {

	private float maxScore = 0;
	private SorterAbstract sort;
	private int rows;
	private int startOffset;
	private SetPriorityQueue priorityQueue;
	private ResultScoreDoc[] sortedDocs;

	public ResultScoreDocPriorityCollector(int rows, SorterAbstract sort,
			ResultScoreDocPriorityCollector previous) {
		this.sort = sort;
		this.rows = rows;
		priorityQueue = null;
		/*
		 * if (previous != null && previous.sort.equals(sort)) { priorityQueue =
		 * new SetPriorityQueue(previous.priorityQueue, rows); startOffset =
		 * previous.rows; maxScore = previous.maxScore; } else {
		 */
		priorityQueue = new SetPriorityQueue(sort, rows);
		startOffset = 0;
		// }
		sortedDocs = null;
	}

	public void collect(ResultScoreDoc[] docs) {
		for (int i = startOffset; i < docs.length; i++) {
			ResultScoreDoc doc = docs[i];
			if (doc.score > maxScore)
				maxScore = doc.score;
			priorityQueue.add(doc);
		}
	}

	final public ResultScoreDoc[] getDocs(Timer timer) {
		if (sortedDocs == null)
			sortedDocs = priorityQueue.getSortedElements(timer);
		return sortedDocs;
	}

	final public float getMaxScore() {
		return maxScore;
	}

	final public boolean match(int rows, SorterAbstract sort) {
		if (this.rows != rows)
			return false;
		return sort.equals(this.sort);
	}

}
