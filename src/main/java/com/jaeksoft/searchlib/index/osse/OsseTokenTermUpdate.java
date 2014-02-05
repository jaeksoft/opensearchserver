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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.api.OsseTransaction;

public class OsseTokenTermUpdate extends AbstractTermFilter {

	private final OsseTermBuffer buffer;
	private final OsseTransaction transaction;
	private final int documentId;
	private final FieldInfo fieldInfo;

	public OsseTokenTermUpdate(final OsseTransaction transaction,
			final int documentId, final FieldInfo fieldInfo,
			final OsseTermBuffer termBuffer, final TokenStream input) {
		super(input);
		this.transaction = transaction;
		this.documentId = documentId;
		this.fieldInfo = fieldInfo;
		this.buffer = termBuffer;
		this.buffer.reset();
	}

	final private void index() throws IOException {
		try {
			if (buffer.termCount == 0)
				return;
			transaction.updateTerms(documentId, fieldInfo, buffer);
			buffer.reset();
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
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

}
