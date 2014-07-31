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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.jaeksoft.searchlib.index.osse.memory.DisposableMemory;
import com.jaeksoft.searchlib.index.osse.memory.MemoryBuffer;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayList;

public class OsseTermBuffer implements Closeable {

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

	final List<OsseTerm> terms;
	final IntBufferedArrayList offsets;
	final IntBufferedArrayList positionIncrements;

	private final MemoryBuffer memoryBuffer;
	private final List<DisposableMemory> byteArrays;

	private DisposableMemory currentByteArray;
	private ByteBuffer currentByteBuffer;

	private final CharsetEncoder encoder;
	private final int maxBytesPerChar;

	public OsseTermBuffer(final MemoryBuffer memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
		this.terms = new ArrayList<OsseTerm>(1);
		this.offsets = new IntBufferedArrayList(1000);
		this.positionIncrements = new IntBufferedArrayList(500);
		this.byteArrays = new ArrayList<DisposableMemory>(1);
		this.encoder = StringUtils.CharsetUTF8.newEncoder();
		this.maxBytesPerChar = (int) encoder.maxBytesPerChar();
		reset();
	}

	public OsseTermBuffer(final MemoryBuffer memoryBuffer, final String term)
			throws IOException {
		this(memoryBuffer);
		addTerm(term);
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		terms.add(new OsseTerm(charArray, charLength));
	}

	final public void addTerm(final String term) throws IOException {
		terms.add(new OsseTerm(term));
	}

	final public void addTerm(final CharTermAttribute termAtt,
			final OffsetAttribute offsetAtt,
			final PositionIncrementAttribute posIncrAtt) throws IOException {
		terms.add(new OsseTerm(termAtt.buffer(), termAtt.length()));

		offsets.add(offsetAtt.startOffset(), offsetAtt.endOffset());
		positionIncrements.add(posIncrAtt.getPositionIncrement());
	}

	final public void reset() {
		close();
		terms.clear();
		offsets.clear();
		positionIncrements.clear();
		byteArrays.clear();
		currentByteArray = null;
		currentByteBuffer = null;
		newByteBuffer(16384);
	}

	@Override
	final public void close() {
		for (DisposableMemory byteArray : byteArrays)
			byteArray.close();
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
		byteArrays.add(currentByteArray);
	}

	final public List<OsseTerm> getTerms() {
		return terms;
	}

	final public int getTermCount() {
		return terms.size();
	}

	final public int getByteArrayCount() {
		return byteArrays.size();
	}

	public boolean hasOffsetOrPosIncr() {
		return offsets.getSize() > 0 || positionIncrements.getSize() > 0;
	}

	final public IntBufferedArrayList getOffsets() {
		return offsets;
	}

	final public IntBufferedArrayList getPositionIncrements() {
		return positionIncrements;
	}

}