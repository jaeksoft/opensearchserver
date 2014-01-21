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
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.index.osse.memory.DisposableMemory;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Pointer;

public class OsseTermBuffer {

	final private static int DEFAULT_BYTEBUFFER_SIZE = 16384;

	final private List<ByteBuffer> byteBuffers;

	final private CharsetEncoder charsetEncoder;

	final private int maxBytesPerChar;

	final private int[] termByteSizes;
	final OsseTermOffset[] offsets;
	final int[] positionIncrements;

	final int bufferSize;

	int termCount;

	private int bytesSize;
	private ByteBuffer currentByteBuffer;

	public OsseTermBuffer(final int bufferSize) {
		this.bufferSize = bufferSize;
		this.charsetEncoder = StringUtils.newUTF8Encoder();
		this.maxBytesPerChar = (int) (charsetEncoder != null ? charsetEncoder
				.maxBytesPerChar() : 1);
		this.byteBuffers = new ArrayList<ByteBuffer>();
		this.termByteSizes = new int[bufferSize];
		this.offsets = OsseTermOffset.getNewArray(bufferSize);
		this.positionIncrements = new int[bufferSize];
		this.currentByteBuffer = null;
		reset();
	}

	public OsseTermBuffer(final String term) throws IOException {
		this(1);
		addTerm(term);
	}

	final private void checkByteBufferSize(int length) {
		if (currentByteBuffer != null && length < currentByteBuffer.remaining())
			return;
		length++;
		byte[] bytes = new byte[length < DEFAULT_BYTEBUFFER_SIZE ? DEFAULT_BYTEBUFFER_SIZE
				: length];
		currentByteBuffer = ByteBuffer.wrap(bytes);
		byteBuffers.add(currentByteBuffer);
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		checkByteBufferSize(charLength * maxBytesPerChar + 1);
		int start = currentByteBuffer.position();
		if (charsetEncoder.encode(CharBuffer.wrap(charArray, 0, charLength),
				currentByteBuffer, false) != CoderResult.UNDERFLOW)
			throw new IOException("Charset encoder underflow condition");
		currentByteBuffer.put((byte) 0);
		int bsize = currentByteBuffer.position() - start;
		termByteSizes[termCount++] = bsize;
		bytesSize += bsize;
	}

	final public void addTerm(final String term) throws IOException {
		checkByteBufferSize(term.length() * maxBytesPerChar + 1);
		int start = currentByteBuffer.position();
		currentByteBuffer.put(term.getBytes(StringUtils.CharsetUTF8));
		currentByteBuffer.put((byte) 0);
		int bsize = currentByteBuffer.position() - start;
		termByteSizes[termCount++] = bsize;
		bytesSize += bsize;
	}

	final public void reset() {
		termCount = 0;
		byteBuffers.clear();
		if (currentByteBuffer != null) {
			currentByteBuffer = ByteBuffer.wrap(currentByteBuffer.array());
			byteBuffers.add(currentByteBuffer);
		}
		bytesSize = 0;
	}

	final public int getTermCount() {
		return termCount;
	}

	final public int getBytesSize() {
		return bytesSize;
	}

	final public void writeTermPointers(final DisposableMemory memory,
			final long bytesBufferMemoryPeer) {
		long pointerOffset = 0;
		long memoryPeerOffset = bytesBufferMemoryPeer;
		for (int i = 0; i < termCount; i++) {
			memory.setPointer(pointerOffset, new Pointer(memoryPeerOffset));
			memoryPeerOffset += termByteSizes[i];
			pointerOffset += Pointer.SIZE;
		}
	}

	final public void writeBytesBuffer(final DisposableMemory memory) {
		long offset = 0;
		for (ByteBuffer byteBuffer : byteBuffers) {
			int length = byteBuffer.position();
			if (length > 0) {
				memory.write(offset, byteBuffer.array(), 0, length);
				offset += length;
			}
		}
	}
}