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

import java.io.IOException;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.sort.PriorityQueue;
import com.jaeksoft.searchlib.sort.SorterAbstract;

public class ResultScoreDocPriorityCollector extends AbstractCollector {

	private float maxScore = 0;
	private SorterAbstract sort;
	private int rows;
	private PriorityQueue priorityQueue;
	private ResultScoreDoc[] sortedDocs;

	public ResultScoreDocPriorityCollector(int rows, SorterAbstract sort) {
		this.sort = sort;
		this.rows = rows;
		priorityQueue = new PriorityQueue(sort, rows);
		sortedDocs = null;
	}

	@Override
	final public void collect(int docId) throws IOException {
		float sc = scorer.score();
		if (sc > maxScore)
			maxScore = sc;
		priorityQueue.add(new ResultScoreDoc(docId, sc));
	}

	final public ResultScoreDoc[] getDocs() {
		if (sortedDocs == null)
			sortedDocs = priorityQueue.getSortedElements();
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
