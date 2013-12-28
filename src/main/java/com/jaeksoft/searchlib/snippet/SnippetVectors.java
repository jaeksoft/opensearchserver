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

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;

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
			final SnippetQueries snippetQueries, final String fieldName)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		if (ArrayUtils.isEmpty(snippetQueries.terms))
			return null;
		TermPositionVector termVector = getTermPositionVector(reader, docId,
				fieldName);
		if (termVector == null)
			return null;
		Collection<SnippetVector> vectors = new ArrayList<SnippetVector>();
		populate(termVector, snippetQueries.terms, vectors);
		vectors = removeIncludes(vectors);
		snippetQueries.checkQueries(vectors);
		vectors = removeNonQuery(vectors);
		return vectors.iterator();
	}

	private static final TermPositionVector getTermPositionVector(
			final ReaderInterface readerInterface, final int docId,
			final String field) throws IOException, SearchLibException {
		TermFreqVector termFreqVector = readerInterface.getTermFreqVector(
				docId, field);
		if (termFreqVector == null)
			return null;
		if (!(termFreqVector instanceof TermPositionVector))
			throw new SearchLibException(
					"Position and offsets has not been set on the field: "
							+ field);
		return (TermPositionVector) termFreqVector;
	}

	private static final void populate(final TermPositionVector termVector,
			final String[] terms, final Collection<SnippetVector> vectors)
			throws SearchLibException {
		int[] termsIdx = termVector.indexesOf(terms, 0, terms.length);
		int i = 0;
		for (int termId : termsIdx) {
			if (termId != -1) {
				TermVectorOffsetInfo[] offsets = termVector.getOffsets(termId);
				int[] positions = termVector.getTermPositions(termId);
				int j = 0;
				for (TermVectorOffsetInfo offset : offsets)
					vectors.add(new SnippetVector(i, offset, positions[j++]));
			}
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
