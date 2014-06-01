/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorOffsetInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.schema.FieldValue;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class IndexDocumentResult {

	public final List<IndexField> fields;

	public IndexDocumentResult(int size) {
		fields = new ArrayList<IndexField>(size);
	}

	public void add(IndexField indexField) {
		fields.add(indexField);
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@JsonInclude(Include.NON_EMPTY)
	public static class IndexField {

		public final String field;
		public final List<String> stored;
		public final List<IndexTerm> terms;

		public IndexField(String field, FieldValue storedFieldValue,
				List<IndexTerm> terms) {
			this.field = field;
			this.stored = storedFieldValue == null ? null : storedFieldValue
					.getValueStringList();
			this.terms = terms;
		}
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@JsonInclude(Include.NON_EMPTY)
	public static class IndexTerm {

		public final String t;
		public final int f;
		public final int[] p;
		public final int[] s;
		public final int[] e;

		private IndexTerm(String term, int frequency, int[] positions,
				TermVectorOffsetInfo[] offsetInfos) {
			this.t = term;
			this.p = positions;
			this.f = frequency;
			if (offsetInfos != null) {
				this.s = new int[offsetInfos.length];
				this.e = new int[offsetInfos.length];
				int i = 0;
				for (TermVectorOffsetInfo offsetInfo : offsetInfos) {
					this.s[i] = offsetInfo.getStartOffset();
					this.e[i] = offsetInfo.getEndOffset();
					i++;
				}
			} else {
				this.s = null;
				this.e = null;
			}
		}

		public final static List<IndexTerm> toList(TermFreqVector termVector) {
			if (termVector == null)
				return null;
			String[] terms = termVector.getTerms();
			if (terms == null)
				return null;
			int[] frequencies = termVector.getTermFrequencies();
			List<IndexTerm> indexTerms = new ArrayList<IndexTerm>(terms.length);
			if (termVector instanceof TermPositionVector)
				toListPosition((TermPositionVector) termVector, terms,
						frequencies, indexTerms);
			else
				toListFreq(termVector, terms, frequencies, indexTerms);
			return indexTerms;
		}

		private final static void toListPosition(TermPositionVector termVector,
				String[] terms, int[] frequencies, List<IndexTerm> indexTerms) {
			int i = 0;
			for (String term : terms) {
				IndexTerm indexTerm = new IndexTerm(term, frequencies[i],
						termVector.getTermPositions(i),
						termVector.getOffsets(i));
				indexTerms.add(indexTerm);
				i++;
			}
		}

		private final static void toListFreq(TermFreqVector termVector,
				String[] terms, int[] frequencies, List<IndexTerm> indexTerms) {
			int i = 0;
			for (String term : terms) {
				IndexTerm indexTerm = new IndexTerm(term, frequencies[i], null,
						null);
				indexTerms.add(indexTerm);
				i++;
			}
		}

		public static List<IndexTerm> toList(ReaderInterface reader,
				String field, int docId) throws SearchLibException, IOException {
			TermEnum te = reader.getTermEnum(new Term(field, ""));
			if (te == null)
				return null;
			List<IndexTerm> indexTerms = new ArrayList<IndexTerm>();
			TermPositions tp = reader.getTermPositions();
			do {
				if (!te.term().field().equals(field))
					break;
				tp.seek(te.term());
				if (!tp.skipTo(docId) || tp.doc() != docId)
					continue;
				String term = te.term().text();
				int[] positions = new int[tp.freq()];
				for (int i = 0; i < positions.length; i++)
					positions[i] = tp.nextPosition();
				IndexTerm indexTerm = new IndexTerm(term, positions.length,
						positions, null);
				indexTerms.add(indexTerm);
			} while (te.next());
			te.close();
			tp.close();
			return indexTerms;
		}
	}

}
