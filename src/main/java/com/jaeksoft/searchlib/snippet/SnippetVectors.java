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

package com.jaeksoft.searchlib.snippet;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.TokenTerm;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.Timer;

class SnippetVectors {

	static class SnippetVector {

		public final int start;
		public final int end;
		public final int term;
		public final int position;
		public boolean remove;
		public boolean query;

		private SnippetVector(final int term,
				final TermVectorOffsetInfo termVectorOffsetInfo,
				final int position) {
			this.term = term;
			this.start = termVectorOffsetInfo.getStartOffset();
			this.end = termVectorOffsetInfo.getEndOffset();
			this.position = position;
			this.remove = false;
			this.query = false;
		}

		@Override
		public String toString() {
			return "Term: " + term + " Start: " + start + " End: " + end
					+ " Pos:" + position;
		}
	}

	final static Iterator<SnippetVector> extractTermVectorIterator(
			final int docId, final ReaderInterface reader,
			final SnippetQueries snippetQueries, final String fieldName,
			List<FieldValueItem> values, CompiledAnalyzer analyzer,
			final Timer parentTimer, final long expiration) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		if (ArrayUtils.isEmpty(snippetQueries.terms))
			return null;

		Timer t = new Timer(parentTimer, "getTermPositionVector " + fieldName);
		TermPositionVector termVector = getTermPositionVector(
				snippetQueries.terms, reader, docId, fieldName, values,
				analyzer, t);
		t.end(null);

		if (termVector == null)
			return null;

		Collection<SnippetVector> vectors = new ArrayList<SnippetVector>();

		t = new Timer(parentTimer, "populate");
		populate(termVector, snippetQueries.terms, vectors, t);
		t.end(null);

		t = new Timer(parentTimer, "removeIncludes");
		vectors = removeIncludes(vectors);
		t.end(null);

		t = new Timer(parentTimer, "checkQueries");
		snippetQueries.checkQueries(vectors, t, expiration);
		t.end(null);

		t = new Timer(parentTimer, "removeNonQuery");
		vectors = removeNonQuery(vectors);
		t.end(null);

		return vectors.iterator();
	}

	private static final TermPositionVector getTermPositionVector(
			final String[] terms, final ReaderInterface readerInterface,
			final int docId, final String field, List<FieldValueItem> values,
			CompiledAnalyzer analyzer, Timer timer) throws IOException,
			SearchLibException, ParseException, SyntaxError {
		TermFreqVector termFreqVector = readerInterface.getTermFreqVector(
				docId, field);
		if (termFreqVector != null)
			if (termFreqVector instanceof TermPositionVector)
				return (TermPositionVector) termFreqVector;
		if (analyzer == null)
			return null;
		SnippetTermPositionVector stpv = new SnippetTermPositionVector(field,
				terms);
		int positionOffset = 0;
		int characterOffset = 0;
		List<TokenTerm> tokenTerms = new ArrayList<TokenTerm>();
		for (FieldValueItem fieldValueItem : values) {
			if (fieldValueItem.value == null)
				continue;
			analyzer.populate(fieldValueItem.value, tokenTerms);
			positionOffset = stpv.addCollection(tokenTerms, characterOffset,
					positionOffset);
			characterOffset += fieldValueItem.value.length() + 1;
			tokenTerms.clear();
		}
		stpv.compile();
		return stpv;
	}

	private static final void populate(final TermPositionVector termVector,
			final String[] terms, final Collection<SnippetVector> vectors,
			Timer parentTimer) throws SearchLibException {
		Timer t = new Timer(parentTimer, "indexesOf");
		int[] termsIdx = termVector.indexesOf(terms, 0, terms.length);
		t.end(null);
		int i = 0;
		for (int termId : termsIdx) {
			Timer termTimer = new Timer(parentTimer, "term " + terms[i]);
			if (termId != -1) {
				t = new Timer(termTimer, "getOffsets");
				TermVectorOffsetInfo[] offsets = termVector.getOffsets(termId);
				t.end(null);
				t = new Timer(termTimer, "getTermPositions");
				int[] positions = termVector.getTermPositions(termId);
				t.end(null);
				t = new Timer(termTimer, "SnippetVector");
				int j = 0;
				for (TermVectorOffsetInfo offset : offsets)
					vectors.add(new SnippetVector(i, offset, positions[j++]));
				t.end(null);
			}
			termTimer.end(null);
			i++;
		}
	}

	private static final Collection<SnippetVector> removeIncludes(
			final Collection<SnippetVector> vectorCollection) {
		SnippetVector[] vectors = vectorCollection
				.toArray(new SnippetVector[vectorCollection.size()]);
		new SnippetVectorSort(vectors);
		SnippetVector last = null;
		for (SnippetVector current : vectors) {
			if (last != null && current.start == last.start
					&& current.end >= last.end)
				last.remove = true;
			last = current;
		}
		List<SnippetVector> vectorList = new ArrayList<SnippetVector>(
				vectors.length);
		for (SnippetVector vector : vectors)
			if (!vector.remove)
				vectorList.add(vector);
		return vectorList;
	}

	private static final Collection<SnippetVector> removeNonQuery(
			final Collection<SnippetVector> vectors) {
		List<SnippetVector> vectorList = new ArrayList<SnippetVector>(
				vectors.size());
		for (SnippetVector vector : vectors)
			if (vector.query)
				vectorList.add(vector);
		return vectorList;
	}

	private static class SnippetVectorSort implements IntComparator, Swapper {

		private final SnippetVector[] vectors;

		private SnippetVectorSort(final SnippetVector[] vectors) {
			this.vectors = vectors;
			Arrays.quickSort(0, vectors.length, this, this);
		}

		@Override
		final public int compare(final Integer k1, final Integer k2) {
			return compare((int) k1, (int) k2);
		}

		@Override
		final public void swap(final int k1, final int k2) {
			SnippetVector v1 = vectors[k1];
			SnippetVector v2 = vectors[k2];
			vectors[k2] = v1;
			vectors[k1] = v2;
		}

		@Override
		final public int compare(final int k1, final int k2) {
			int i = vectors[k1].start - vectors[k2].start;
			if (i == 0)
				i = vectors[k1].end - vectors[k2].end;
			return i;
		}

	}

}
