/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse.memory;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractBuffer<T extends BufferItemInterface> implements
		Closeable {

	private TreeMap<Integer, ArrayDeque<T>> available;

	public AbstractBuffer() {
		available = new TreeMap<Integer, ArrayDeque<T>>();
	}

	@Override
	final public void close() {
		available.clear();
	}

	protected abstract T newBufferItem(final int size);

	@SuppressWarnings("unchecked")
	final public T getNewBufferItem(final int size) {
		Map.Entry<Integer, ArrayDeque<T>> entry = available.ceilingEntry(size);
		if (entry == null)
			return newBufferItem(size);
		ArrayDeque<T> memoryQue = entry.getValue();
		if (memoryQue.isEmpty())
			return newBufferItem(size);
		return (T) memoryQue.poll().reset();
	}

	final void closed(T bufferItem) {
		final int size = bufferItem.getSize();
		ArrayDeque<T> memoryQue = available.get(size);
		if (memoryQue == null) {
			memoryQue = new ArrayDeque<T>();
			available.put(size, memoryQue);
		}
		memoryQue.offer(bufferItem);
	}

}
