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

package com.jaeksoft.searchlib.sort.priorityQueue;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.Timer;

public abstract class AbstractPriorityQueue<T extends AbstractPriorityQueue<?>> {

	protected interface AdderInterface {

		public AdderInterface add(ResultScoreDoc doc);
	}

	protected class UniqueAdder implements AdderInterface {

		protected ResultScoreDoc uniqueDoc;

		protected UniqueAdder(ResultScoreDoc doc) {
			uniqueDoc = doc;
		}

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			if (sorter.compare(doc, uniqueDoc) > 0)
				return this;
			uniqueDoc = doc;
			return this;
		}
	}

	final protected SorterAbstract sorter;

	final protected int capacity;

	protected AdderInterface currentAdder;

	public AbstractPriorityQueue(SorterAbstract sorter, int capacity) {
		this.sorter = sorter;
		this.capacity = capacity;
	}

	public AbstractPriorityQueue(T previous, int newIncreasedCapacity) {
		this.sorter = previous.sorter;
		this.capacity = newIncreasedCapacity > previous.capacity ? newIncreasedCapacity
				: previous.capacity;
	}

	final public void add(ResultScoreDoc doc) {
		currentAdder = currentAdder.add(doc);
	}

	public abstract int getSize();

	public abstract ResultScoreDoc[] getSortedElements(Timer timer);

}
