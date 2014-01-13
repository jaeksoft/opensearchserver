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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.api.OsseTransaction;

public class OsseTokenTermUpdate extends AbstractTermFilter {

	private final OsseTermBuffer buffer;
	private final OsseTransaction transaction;
	private final int documentId;
	private final FieldInfo field;
	private final CharsetEncoder encoder;

	public OsseTokenTermUpdate(final OsseTransaction transaction,
			final int documentId, final FieldInfo field,
			final OsseTermBuffer termBuffer, final TokenStream input,
			final CharsetEncoder encoder) {
		super(input);
		this.transaction = transaction;
		this.documentId = documentId;
		this.field = field;
		this.buffer = termBuffer;
		this.encoder = encoder;
		this.buffer.reset();
	}

	final private void index() throws IOException {
		if (buffer.length == 0)
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
			OsseTerm term = new OsseTerm(encoder, termAtt.buffer(),
					termAtt.length());
			final OsseTermOffset offset = buffer.offsets[buffer.length];
			offset.ui32StartOffset = offsetAtt.startOffset();
			offset.ui32EndOffset = offsetAtt.endOffset();
			buffer.positionIncrements[buffer.length] = posIncrAtt
					.getPositionIncrement();
			buffer.add(term);
			if (buffer.length == buffer.bufferSize) {
				index();
				return true;
			}
		}
	}

	@Override
	final public void close() throws IOException {
		super.close();
	}

	public static class OsseTerm {

		public final byte[] bytes;

		private OsseTerm(final CharsetEncoder charsetEncoder,
				final char[] buffer, final int length)
				throws CharacterCodingException {
			charsetEncoder.reset();
			bytes = charsetEncoder.encode(CharBuffer.wrap(buffer, 0, length))
					.array();
		}

		public OsseTerm(final CharsetEncoder charsetEncoder, final String term)
				throws CharacterCodingException {
			charsetEncoder.reset();
			bytes = charsetEncoder.encode(CharBuffer.wrap(term)).array();
		}

	}

	public static class OsseTermBuffer {

		public final OsseTerm[] terms;
		public final OsseTermOffset[] offsets;
		public final int[] positionIncrements;
		private final int bufferSize;
		public int length;
		public int bytesSize;

		public OsseTermBuffer(final int bufferSize) {
			this.bufferSize = bufferSize;
			terms = new OsseTerm[bufferSize];
			offsets = OsseTermOffset.getNewArray(bufferSize);
			positionIncrements = new int[bufferSize];
		}

		public OsseTermBuffer(final CharsetEncoder encoder, final String term)
				throws CharacterCodingException {
			this(1);
			add(new OsseTerm(encoder, term));
		}

		final public void add(final OsseTerm term) {
			terms[length++] = term;
			bytesSize += term.bytes.length;
		}

		final public void reset() {
			length = 0;
			bytesSize = 0;
		}

	}

}
