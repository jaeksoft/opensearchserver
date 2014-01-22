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
import java.nio.CharBuffer;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.TreeMap;

public class CharArrayBuffer implements Closeable {

	public class CharArray implements Closeable {

		final public char[] chars;
		public CharBuffer charBuffer;

		private CharArray(final int size) {
			chars = new char[size];
			reset();
		}

		private final CharArray reset() {
			charBuffer = CharBuffer.wrap(chars);
			return this;
		}

		@Override
		final public void close() {
			closed(this);
		}
	}

	private long reused;
	private TreeMap<Integer, ArrayDeque<CharArray>> available;

	public CharArrayBuffer() {
		available = new TreeMap<Integer, ArrayDeque<CharArray>>();
		reused = 0;
	}

	@Override
	final public void close() {
		System.out.println("CHARARRAY RELEASE " + available.size() + " "
				+ reused);
		available.clear();
		reused = 0;
	}

	final public CharArray getCharArray(final int size) {
		Map.Entry<Integer, ArrayDeque<CharArray>> entry = available
				.ceilingEntry(size);
		if (entry == null)
			return new CharArray(size);
		ArrayDeque<CharArray> memoryQue = entry.getValue();
		if (memoryQue.isEmpty())
			return new CharArray(size);
		reused++;
		return memoryQue.poll().reset();

	}

	final private void closed(CharArray charArray) {
		ArrayDeque<CharArray> charArrayQue = available
				.get(charArray.chars.length);
		if (charArrayQue == null) {
			charArrayQue = new ArrayDeque<CharArray>();
			available.put(charArray.chars.length, charArrayQue);
		}
		charArrayQue.offer(charArray);
	}

}
