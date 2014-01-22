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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import com.jaeksoft.searchlib.index.osse.memory.ByteArray;
import com.jaeksoft.searchlib.index.osse.memory.ByteArrayBuffer;
import com.jaeksoft.searchlib.util.StringUtils;

public class OsseTermBuffer {

	final OsseTermOffset[] offsets;
	final int[] termLengths;
	final int[] positionIncrements;
	final int bufferSize;
	int totalBytesLength;
	int termCount;

	// private final CharArrayBuffer charArrayBuffer;
	// private final CharArray[] charArrays;
	// private CharBuffer currentCharBuffer;
	// private int charArrayCount;

	private final ByteArrayBuffer byteArrayBuffer;
	private final ByteArray[] byteArrays;
	private ByteBuffer currentByteBuffer;
	private int byteArrayCount;

	private final CharsetEncoder encoder;
	private final int maxBytesPerChar;

	public OsseTermBuffer(final ByteArrayBuffer byteArrayBuffer,
			final int bufferSize) {
		this.byteArrayBuffer = byteArrayBuffer;
		this.bufferSize = bufferSize;
		this.termLengths = new int[bufferSize];
		this.offsets = OsseTermOffset.getNewArray(bufferSize);
		this.positionIncrements = new int[bufferSize];
		this.byteArrays = new ByteArray[bufferSize];
		this.encoder = StringUtils.CharsetUTF8.newEncoder();
		this.maxBytesPerChar = (int) encoder.maxBytesPerChar();
		reset();
	}

	public OsseTermBuffer(final ByteArrayBuffer byteArrayBuffer,
			final String term) throws IOException {
		this(byteArrayBuffer, 1);
		addTerm(term);
	}

	// final public void addTerm(final char[] charArray, final int charLength)
	// throws IOException {
	// final int fullLength = charLength + 1;
	// if (fullLength > currentCharBuffer.remaining())
	// newCharBuffer(fullLength);
	// currentCharBuffer.put(charArray, 0, charLength);
	// currentCharBuffer.put((char) 0);
	// totalCharLength += fullLength;
	// termLengths[termCount++] = fullLength;
	// }

	// final public void addTerm(final String term) throws IOException {
	// final int termLength = term.length();
	// final int fullLength = termLength + 1;
	// if (fullLength > currentCharBuffer.remaining())
	// newCharBuffer(fullLength);
	// int position = currentCharBuffer.position();
	// term.getChars(0, termLength, currentCharBuffer.array(), position);
	// currentCharBuffer.position(position + termLength);
	// currentCharBuffer.put((char) 0);
	// totalCharLength += fullLength;
	// termLengths[termCount++] = fullLength;
	// }

	final private int checkBufferAndGetStartPos(final int charLength) {
		final int fullLength = charLength * maxBytesPerChar + 1;
		if (fullLength > currentByteBuffer.remaining())
			newByteBuffer(fullLength);
		return currentByteBuffer.position();
	}

	final private void finalizeNextTerm(final int startPos) {
		currentByteBuffer.put((byte) 0);
		int length = currentByteBuffer.position() - startPos;
		totalBytesLength += length;
		termLengths[termCount++] = length;
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		int pos = checkBufferAndGetStartPos(charLength);
		if (encoder.encode(CharBuffer.wrap(charArray, 0, charLength),
				currentByteBuffer, false) != CoderResult.UNDERFLOW)
			throw new IOException("Charset Encoder issue");
		finalizeNextTerm(pos);
	}

	final public void addTerm(final String term) throws IOException {
		int pos = checkBufferAndGetStartPos(term.length());
		currentByteBuffer.put(term.getBytes(StringUtils.CharsetUTF8));
		finalizeNextTerm(pos);
	}

	final public void reset() {
		release();
		termCount = 0;
		totalBytesLength = 0;
		currentByteBuffer = null;
		newByteBuffer(16384);
	}

	final public void release() {
		for (int i = 0; i < byteArrayCount; i++)
			byteArrays[i].close();
		byteArrayCount = 0;
	}

	final private void newByteBuffer(final int length) {
		boolean bNew = currentByteBuffer == null
				|| currentByteBuffer.position() > 0;
		ByteArray byteArray = byteArrayBuffer
				.getNewBufferItem(length < 16384 ? 16384 : length);
		currentByteBuffer = byteArray.byteBuffer;
		byteArrays[byteArrayCount] = byteArray;
		if (bNew)
			byteArrayCount++;
	}

	final public int getTermCount() {
		return termCount;
	}

	final public int[] getTermLengths() {
		return termLengths;
	}

	final public int getTotalBytesLength() {
		return totalBytesLength;
	}

	final public ByteArray[] getByteArrays() {
		return byteArrays;
	}

	final public int getByteArrayCount() {
		return byteArrayCount;
	}

}