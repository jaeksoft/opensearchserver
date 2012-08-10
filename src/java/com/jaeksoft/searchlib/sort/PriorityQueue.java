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

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.util.Timer;

public class PriorityQueue {

	private interface AdderInterface {

		public AdderInterface add(ResultScoreDoc doc);
	}

	private class EmptyAdder implements AdderInterface {

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			array[0] = doc;
			min = doc;
			max = doc;
			return capacity == 1 ? new UniqueAdder() : new NonFullAdder(1);
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
				sorter.sort(array);
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
			sorter.sort(array);
			max = array[loopEnd];
			return this;
		}

	}

	private class UniqueAdder implements AdderInterface {

		@Override
		final public AdderInterface add(ResultScoreDoc doc) {
			if (sorter.compare(doc, array[0]) > 0)
				return this;
			array[0] = doc;
			return this;
		}
	}

	final private SorterAbstract sorter;

	final private ResultScoreDoc[] array;

	private ResultScoreDoc min;

	private ResultScoreDoc max;

	final private int capacity;

	private AdderInterface currentAdder;

	public PriorityQueue(SorterAbstract sorter, int capacity) {
		this.sorter = sorter;
		this.capacity = capacity;
		array = new ResultScoreDoc[capacity];
		currentAdder = new EmptyAdder();
	}

	public PriorityQueue(PriorityQueue previous, int newIncreasedCapacity) {
		this.sorter = previous.sorter;
		this.capacity = newIncreasedCapacity > previous.capacity ? newIncreasedCapacity
				: previous.capacity;
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

	final public void add(ResultScoreDoc doc) {
		currentAdder = currentAdder.add(doc);
	}

	final public int getSize() {
		int size = 0;
		for (ResultScoreDoc doc : array)
			if (doc != null)
				size++;
		return size;
	}

	public ResultScoreDoc[] getSortedElements(Timer timer) {
		int size = getSize();
		ResultScoreDoc[] docs = new ResultScoreDoc[size];
		size = 0;
		for (ResultScoreDoc doc : array)
			if (doc != null)
				docs[size++] = doc;
		sorter.sort(docs, timer);
		return docs;
	}

}
