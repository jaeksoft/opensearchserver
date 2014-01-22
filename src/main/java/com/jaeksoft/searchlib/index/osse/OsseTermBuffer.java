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

package com.jaeksoft.searchlib.index.osse;

import java.io.IOException;
import java.nio.CharBuffer;

import com.jaeksoft.searchlib.index.osse.memory.CharArrayBuffer;
import com.jaeksoft.searchlib.index.osse.memory.CharArrayBuffer.CharArray;

public class OsseTermBuffer {

	final OsseTermOffset[] offsets;
	final int[] termLengths;
	final int[] positionIncrements;
	final int bufferSize;
	long totalCharLength;
	int termCount;

	private final CharArrayBuffer charArrayBuffer;
	private final CharArray[] charArrays;
	private CharBuffer currentCharBuffer;
	private int charArrayCount;

	public OsseTermBuffer(final CharArrayBuffer charArrayBuffer,
			final int bufferSize) {
		this.charArrayBuffer = charArrayBuffer;
		this.bufferSize = bufferSize;
		this.termLengths = new int[bufferSize];
		this.offsets = OsseTermOffset.getNewArray(bufferSize);
		this.positionIncrements = new int[bufferSize];
		this.charArrays = new CharArray[bufferSize];
		reset();
	}

	public OsseTermBuffer(final CharArrayBuffer charArrayBuffer,
			final String term) throws IOException {
		this(charArrayBuffer, 1);
		addTerm(term);
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		final int fullLength = charLength + 1;
		if (fullLength > currentCharBuffer.remaining())
			newCharBuffer(fullLength);
		currentCharBuffer.put(charArray, 0, charLength);
		currentCharBuffer.put((char) 0);
		totalCharLength += fullLength;
		termLengths[termCount++] = fullLength;
	}

	final public void addTerm(final String term) throws IOException {
		final int termLength = term.length();
		final int fullLength = termLength + 1;
		if (fullLength > currentCharBuffer.remaining())
			newCharBuffer(fullLength);
		int position = currentCharBuffer.position();
		term.getChars(0, termLength, currentCharBuffer.array(), position);
		currentCharBuffer.position(position + termLength);
		currentCharBuffer.put((char) 0);
		totalCharLength += fullLength;
		termLengths[termCount++] = fullLength;
	}

	final public void reset() {
		release();
		termCount = 0;
		totalCharLength = 0;
		currentCharBuffer = null;
		newCharBuffer(16384);
	}

	final public void release() {
		for (int i = 0; i < charArrayCount; i++)
			charArrays[i].close();
		charArrayCount = 0;
	}

	final private void newCharBuffer(final int length) {
		boolean bNew = currentCharBuffer == null
				|| currentCharBuffer.position() > 0;
		CharArray charArray = charArrayBuffer
				.getCharArray(length < 16384 ? 16384 : length);
		currentCharBuffer = charArray.charBuffer;
		charArrays[charArrayCount] = charArray;
		if (bNew)
			charArrayCount++;
	}

	final public int getTermCount() {
		return termCount;
	}

	final public int[] getTermLengths() {
		return termLengths;
	}

	final public long getTotalCharLength() {
		return totalCharLength;
	}

	final public CharArray[] getCharArrays() {
		return charArrays;
	}

	final public int getCharArrayCount() {
		return charArrayCount;
	}

}