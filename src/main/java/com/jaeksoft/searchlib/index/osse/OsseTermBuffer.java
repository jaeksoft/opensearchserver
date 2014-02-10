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

import com.jaeksoft.searchlib.index.osse.memory.DisposableMemory;
import com.jaeksoft.searchlib.index.osse.memory.MemoryBuffer;
import com.jaeksoft.searchlib.util.StringUtils;

public class OsseTermBuffer {

	public final class OsseTerm {

		public final DisposableMemory memory;
		public final int offset;

		private OsseTerm(final int charLength) {
			checkByteBuffer(charLength);
			this.memory = currentByteArray;
			this.offset = currentByteBuffer.position();
		}

		private OsseTerm(final char[] charArray, final int charLength)
				throws IOException {
			this(charLength);
			if (encoder.encode(CharBuffer.wrap(charArray, 0, charLength),
					currentByteBuffer, false) != CoderResult.UNDERFLOW)
				throw new IOException("Charset Encoder issue");
			currentByteBuffer.put((byte) 0);
		}

		private OsseTerm(String term) throws IOException {
			this(term.length());
			try {
				currentByteBuffer.put(term.getBytes(StringUtils.CharsetUTF8));
				currentByteBuffer.put((byte) 0);
			} catch (java.nio.BufferOverflowException e) {
				throw e;
			}
		}
	}

	final OsseTermOffset[] offsets;
	final OsseTerm[] terms;
	final int[] positionIncrements;
	final int bufferSize;
	int termCount;

	private final MemoryBuffer memoryBuffer;
	private final DisposableMemory[] byteArrays;

	private int byteArrayCount;
	private DisposableMemory currentByteArray;
	private ByteBuffer currentByteBuffer;

	private final CharsetEncoder encoder;
	private final int maxBytesPerChar;

	public OsseTermBuffer(final MemoryBuffer memoryBuffer, final int bufferSize) {
		this.memoryBuffer = memoryBuffer;
		this.bufferSize = bufferSize;
		this.terms = new OsseTerm[bufferSize];
		this.offsets = OsseTermOffset.getNewArray(bufferSize);
		this.positionIncrements = new int[bufferSize];
		this.byteArrays = new DisposableMemory[bufferSize];
		this.encoder = StringUtils.CharsetUTF8.newEncoder();
		this.maxBytesPerChar = (int) encoder.maxBytesPerChar();
		this.byteArrayCount = 0;
		reset();
	}

	public OsseTermBuffer(final MemoryBuffer memoryBuffer, final String term)
			throws IOException {
		this(memoryBuffer, 1);
		addTerm(term);
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		terms[termCount++] = new OsseTerm(charArray, charLength);
	}

	final public void addTerm(final String term) throws IOException {
		terms[termCount++] = new OsseTerm(term);
	}

	final public void reset() {
		release();
		termCount = 0;
		currentByteArray = null;
		currentByteBuffer = null;
		newByteBuffer(16384);
	}

	final public void release() {
		for (int i = 0; i < byteArrayCount; i++) {
			DisposableMemory byteArray = byteArrays[i];
			if (byteArray == null)
				break;
			byteArray.close();
			byteArrays[i] = null;
		}
		byteArrayCount = 0;
	}

	final private void checkByteBuffer(final int charLength) {
		final int fullLength = charLength * maxBytesPerChar + 1;
		if (fullLength > currentByteBuffer.remaining())
			newByteBuffer(fullLength);
	}

	final private void newByteBuffer(final int length) {
		currentByteArray = memoryBuffer.getNewBufferItem(length < 16384 ? 16384
				: length);
		currentByteBuffer = currentByteArray.getByteBuffer();
		byteArrays[byteArrayCount++] = currentByteArray;
	}

	final public OsseTerm[] getTerms() {
		return terms;
	}

	final public int getTermCount() {
		return termCount;
	}

	final public int getByteArrayCount() {
		return byteArrayCount;
	}

}