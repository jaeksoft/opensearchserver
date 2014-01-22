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
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;

import com.jaeksoft.searchlib.index.osse.OsseTermBuffer;
import com.jaeksoft.searchlib.index.osse.memory.CharArrayBuffer.CharArray;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * This class implements a fast UTF-8 String array *
 */
public class OsseFastStringArray extends Pointer implements Closeable {

	/**
	 * Optimized write only StringArray
	 * 
	 * @param strings
	 */

	private final DisposableMemory termPointers;
	private final DisposableMemory fullBytes;

	public OsseFastStringArray(final MemoryBuffer memoryBuffer,
			final OsseTermBuffer termBuffer)
			throws UnsupportedEncodingException, CharacterCodingException {
		super(0);

		// First we reserve memory for the list of pointers
		final int termCount = termBuffer.getTermCount();
		termPointers = memoryBuffer.getMemory(termCount * Pointer.SIZE);
		peer = termPointers.getPeer();

		// Filling the characters memory
		fullBytes = memoryBuffer.getMemory(termBuffer.getTotalCharLength()
				* Native.WCHAR_SIZE);
		long offset = 0;
		int charArrayCount = termBuffer.getCharArrayCount();
		CharArray[] charArrays = termBuffer.getCharArrays();
		for (int i = 0; i < charArrayCount; i++) {
			CharBuffer charBuffer = charArrays[i].charBuffer;
			int length = charBuffer.position();
			fullBytes.write(offset, charBuffer.array(), 0, length);
			offset += length * Native.WCHAR_SIZE;
		}

		// Filling the pointer array memory
		long stringPeer = fullBytes.getPeer();
		int[] termLengths = termBuffer.getTermLengths();
		Pointer[] pointers = new Pointer[termCount];
		for (int i = 0; i < termCount; i++) {
			pointers[i] = new Pointer(stringPeer);
			stringPeer += termLengths[i] * Native.WCHAR_SIZE;
		}
		termPointers.write(0, pointers, 0, termCount);
	}

	@Override
	final public void close() {
		fullBytes.close();
		termPointers.close();
	}

	@Override
	public String toString() {
		return StringUtils.fastConcat("[", super.toString(), " ",
				fullBytes.toString(), "]");
	}
}
