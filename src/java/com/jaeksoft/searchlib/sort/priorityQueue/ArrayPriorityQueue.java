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

public class ArrayPriorityQueue extends
		AbstractPriorityQueue<ArrayPriorityQueue> {

	private class EmptyAdder implements AdderInterface {

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			array[0] = doc;
			min = doc;
			max = doc;
			return capacity == 1 ? new UniqueAdder(doc) : new NonFullAdder(1);
		}
	}

	private class NonFullAdder implements AdderInterface {

		private int currentPos;

		final private int loopEnd = capacity - 1;

		private NonFullAdder(int initialPos) {
			currentPos = initialPos;
		}

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			array[currentPos++] = doc;
			if (currentPos == capacity) {
				sorter.arraySort(array);
				min = array[0];
				max = array[loopEnd];
				return new FullAdder();
			}
			return this;
		}

	}

	private class FullAdder implements AdderInterface {

		final private int loopEnd = capacity - 1;

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			int c = sorter.compare(doc, max);
			if (c > 0)
				return this;
			c = sorter.compare(doc, min);
			if (c <= 0) {
				int i = loopEnd;

				while (i != 0)
					array[i] = array[--i];
				array[0] = doc;
				min = doc;
				return this;
			}
			array[loopEnd] = doc;
			sorter.arraySort(array);
			max = array[loopEnd];
			return this;
		}

	}

	final private ResultScoreDoc[] array;

	private ResultScoreDoc min;

	private ResultScoreDoc max;

	public ArrayPriorityQueue(SorterAbstract sorter, int capacity) {
		super(sorter, capacity);
		array = new ResultScoreDoc[capacity];
		currentAdder = new EmptyAdder();
	}

	public ArrayPriorityQueue(ArrayPriorityQueue previous,
			int newIncreasedCapacity) {
		super(previous, newIncreasedCapacity);
		if (this.capacity > previous.capacity) {
			array = new ResultScoreDoc[capacity];
			int i = 0;
			for (ResultScoreDoc doc : previous.array)
				array[i++] = doc;
			currentAdder = new NonFullAdder(previous.getSize());
		} else {
			array = previous.array;
			currentAdder = previous.currentAdder;
		}
		min = previous.min;
		max = previous.max;

	}

	@Override
	final public int getSize() {
		if (currentAdder instanceof AbstractPriorityQueue<?>.UniqueAdder) {
			@SuppressWarnings("rawtypes")
			AbstractPriorityQueue<?>.UniqueAdder uniqueAdder = (AbstractPriorityQueue.UniqueAdder) currentAdder;
			array[0] = uniqueAdder.uniqueDoc;
		}
		int size = 0;
		for (ResultScoreDoc doc : array)
			if (doc != null)
				size++;
		return size;
	}

	@Override
	public ResultScoreDoc[] getSortedElements(Timer timer) {
		int size = getSize();
		ResultScoreDoc[] docs = new ResultScoreDoc[size];
		size = 0;
		for (ResultScoreDoc doc : array)
			if (doc != null)
				docs[size++] = doc;
		sorter.quickSort(docs, timer);
		return docs;
	}

}
