/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

public class MemoryBuffer implements Closeable {

	private long reused;
	private TreeMap<Long, ArrayDeque<DisposableMemory>> available;

	public MemoryBuffer() {
		available = new TreeMap<Long, ArrayDeque<DisposableMemory>>();
		reused = 0;
	}

	@Override
	public void close() {
		System.out.println("MEMORY RELEASE " + available.size() + " " + reused);
		available.clear();
		reused = 0;
	}

	final private DisposableMemory newMemory(final long size) {
		return new DisposableMemory(this, size);
	}

	final public DisposableMemory getMemory(final long size) {
		Map.Entry<Long, ArrayDeque<DisposableMemory>> entry = available
				.ceilingEntry(size);
		if (entry == null)
			return newMemory(size);
		ArrayDeque<DisposableMemory> memoryQue = entry.getValue();
		if (memoryQue.isEmpty())
			return newMemory(size);
		reused++;
		return memoryQue.poll();

	}

	void closed(DisposableMemory memory) {
		ArrayDeque<DisposableMemory> memoryQue = available.get(memory.size);
		if (memoryQue == null) {
			memoryQue = new ArrayDeque<DisposableMemory>();
			available.put(memory.size, memoryQue);
		}
		memoryQue.offer(memory);
	}

}
