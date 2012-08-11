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

import java.util.TreeSet;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.sort.AscDocIdSorter;
import com.jaeksoft.searchlib.sort.MultiSort;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.Timer;

public class SetPriorityQueue extends AbstractPriorityQueue<SetPriorityQueue> {

	private class EmptyAdder implements AdderInterface {

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			set.add(doc);
			max = doc;
			return capacity == 1 ? new UniqueAdder(doc) : new NonFullAdder(1);
		}
	}

	private class NonFullAdder implements AdderInterface {

		private int currentPos;

		private NonFullAdder(int initialPos) {
			currentPos = initialPos;
		}

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			set.add(doc);
			if (++currentPos == capacity) {
				max = set.last();
				System.out.println("NonFull End " + set.size() + " / "
						+ capacity);
				return new FullAdder();
			}
			return this;
		}

	}

	private class FullAdder implements AdderInterface {

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			int c = sorter.compare(doc, max);
			if (c > 0)
				return this;
			set.remove(max);
			set.add(doc);
			max = set.last();
			if (set.size() != capacity)
				System.out.println("Hey ! " + doc + " " + set.size() + " / "
						+ capacity);
			return this;
		}

	}

	final private TreeSet<ResultScoreDoc> set;

	private ResultScoreDoc max;

	public SetPriorityQueue(SorterAbstract sorter, int capacity) {
		super(new MultiSort(sorter, new AscDocIdSorter()), capacity);
		set = new TreeSet<ResultScoreDoc>(this.sorter);
		currentAdder = new EmptyAdder();
	}

	public SetPriorityQueue(SetPriorityQueue previous, int newIncreasedCapacity) {
		super(previous, newIncreasedCapacity);
		set = previous.set;
		if (this.capacity > previous.capacity) {
			currentAdder = new NonFullAdder(previous.getSize());
		} else {
			currentAdder = previous.currentAdder;
		}
		max = previous.max;

	}

	@Override
	final public int getSize() {
		if (currentAdder instanceof AbstractPriorityQueue<?>.UniqueAdder) {
			set.clear();
			set.add(((AbstractPriorityQueue<?>.UniqueAdder) currentAdder).uniqueDoc);
		}
		return set.size();
	}

	@Override
	public ResultScoreDoc[] getSortedElements(Timer timer) {
		int size = getSize();
		System.out.println("End with : " + size + " / " + capacity);

		ResultScoreDoc[] docs = new ResultScoreDoc[size];
		set.toArray(docs);
		return docs;
	}
}
