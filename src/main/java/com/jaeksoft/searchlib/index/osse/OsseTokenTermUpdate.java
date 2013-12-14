/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;
import com.jaeksoft.searchlib.index.osse.OsseFieldList.FieldInfo;

public class OsseTokenTermUpdate extends AbstractTermFilter {

	private final TermBuffer buffer;
	private final OsseTransaction transaction;
	private final int documentId;
	private final FieldInfo field;
	private int length;

	public OsseTokenTermUpdate(final OsseTransaction transaction,
			final int documentId, final FieldInfo field, TermBuffer termBuffer,
			final TokenStream input) {
		super(input);
		this.transaction = transaction;
		this.documentId = documentId;
		this.field = field;
		this.buffer = termBuffer;
		length = 0;
	}

	final private void index() throws IOException {
		if (length == 0)
			return;
		try {
			transaction.updateTerms(documentId, field, buffer, length);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
		length = 0;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (!input.incrementToken()) {
			index();
			return false;
		}
		buffer.terms[length] = termAtt.toString();
		final OsseTermOffset offset = buffer.offsets[length];
		offset.ui32StartOffset = offsetAtt.startOffset();
		offset.ui32EndOffset = offsetAtt.endOffset();
		buffer.positionIncrements[length] = posIncrAtt.getPositionIncrement();
		if (++length == buffer.bufferSize)
			index();
		return true;
	}

	@Override
	final public void close() throws IOException {
		super.close();
	}

	public static class TermBuffer {

		public final String[] terms;
		public final OsseTermOffset[] offsets;
		public final int[] positionIncrements;
		public final int bufferSize;

		public TermBuffer(final int bufferSize) {
			this.bufferSize = bufferSize;
			terms = new String[bufferSize];
			offsets = OsseTermOffset.getNewArray(bufferSize);
			positionIncrements = new int[bufferSize];
		}

	}

}
