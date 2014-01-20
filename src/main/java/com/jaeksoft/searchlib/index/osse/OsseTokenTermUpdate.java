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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.api.OsseTransaction;
import com.jaeksoft.searchlib.index.osse.memory.DisposableMemory;
import com.sun.jna.Pointer;

public class OsseTokenTermUpdate extends AbstractTermFilter {

	private final OsseTermBuffer buffer;
	private final OsseTransaction transaction;
	private final int documentId;
	private final FieldInfo field;

	public OsseTokenTermUpdate(final OsseTransaction transaction,
			final int documentId, final FieldInfo field,
			final OsseTermBuffer termBuffer, final TokenStream input) {
		super(input);
		this.transaction = transaction;
		this.documentId = documentId;
		this.field = field;
		this.buffer = termBuffer;
		this.buffer.reset();
	}

	final private void index() throws IOException {
		if (buffer.getTermCount() == 0)
			return;
		try {
			transaction.updateTerms(documentId, field, buffer);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
		buffer.reset();
	}

	@Override
	public final boolean incrementToken() throws IOException {
		for (;;) {
			if (!input.incrementToken()) {
				index();
				return false;
			}
			final OsseTermOffset offset = buffer.offsets[buffer.termCount];
			offset.ui32StartOffset = offsetAtt.startOffset();
			offset.ui32EndOffset = offsetAtt.endOffset();
			buffer.positionIncrements[buffer.termCount] = posIncrAtt
					.getPositionIncrement();
			buffer.addTerm(termAtt.buffer(), termAtt.length());
			if (buffer.termCount == buffer.bufferSize) {
				index();
				return true;
			}
		}
	}

	@Override
	final public void close() throws IOException {
		super.close();
	}

	public static class OsseTermBuffer {

		final private static int DEFAULT_BYTEBUFFER_SIZE = 16384;

		final private List<ByteBuffer> byteBuffers;

		final private CharsetEncoder encoder;
		final private int maxBytesPerChar;

		final private int[] termByteSizes;
		final private OsseTermOffset[] offsets;
		final private int[] positionIncrements;

		private final int bufferSize;
		private int termCount;
		private int bytesSize;
		private ByteBuffer currentByteBuffer;

		public OsseTermBuffer(final CharsetEncoder encoder, final int bufferSize) {
			this.encoder = encoder;
			this.maxBytesPerChar = (int) (encoder != null ? encoder
					.maxBytesPerChar() : 1);
			this.byteBuffers = new ArrayList<ByteBuffer>();
			this.bufferSize = bufferSize;
			this.termByteSizes = new int[bufferSize];
			this.offsets = OsseTermOffset.getNewArray(bufferSize);
			this.positionIncrements = new int[bufferSize];
			this.currentByteBuffer = null;
			reset();
		}

		public OsseTermBuffer(final CharsetEncoder encoder, final String term)
				throws IOException {
			this(encoder, 1);
			addTerm(term);
		}

		final private void checkByteBufferSize(int length) {
			if (currentByteBuffer != null
					&& length < currentByteBuffer.remaining())
				return;
			encoder.reset();
			length++;
			currentByteBuffer = ByteBuffer
					.allocate(length < DEFAULT_BYTEBUFFER_SIZE ? DEFAULT_BYTEBUFFER_SIZE
							: length);
			byteBuffers.add(currentByteBuffer);
		}

		final public void addTerm(final CharBuffer charBuffer, int charLength)
				throws IOException {
			checkByteBufferSize(charLength * maxBytesPerChar + 1);
			int start = currentByteBuffer.position();
			if (encoder.encode(charBuffer, currentByteBuffer, false) != CoderResult.UNDERFLOW)
				throw new IOException("Charset encoder underflow condition");
			currentByteBuffer.put((byte) 0);
			int bsize = currentByteBuffer.position() - start;
			termByteSizes[termCount++] = bsize;
			bytesSize += bsize;
		}

		final public void addTerm(final char[] buffer, final int length)
				throws IOException {
			addTerm(CharBuffer.wrap(buffer, 0, length), length);
		}

		final public void addTerm(final String term) throws IOException {
			addTerm(CharBuffer.wrap(term), term.length());
		}

		final public void reset() {
			encoder.reset();
			termCount = 0;
			byteBuffers.clear();
			if (currentByteBuffer != null) {
				currentByteBuffer.clear();
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

}
