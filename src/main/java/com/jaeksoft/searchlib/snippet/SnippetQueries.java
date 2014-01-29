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

package com.jaeksoft.searchlib.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.snippet.SnippetVectors.SnippetVector;
import com.jaeksoft.searchlib.util.Timer;

class SnippetQueries {

	private final String field;
	private final Map<String, Integer> termMap;
	private final List<String> termList;
	private final Set<Integer> termQuerySet;
	private final Set<Integer> termPhraseSet;
	private final List<TermSequence> termSequenceList;
	final String[] terms;

	SnippetQueries(final Query query, final String field) {
		this.field = field;
		termQuerySet = new TreeSet<Integer>();
		termPhraseSet = new TreeSet<Integer>();
		termMap = new TreeMap<String, Integer>();
		termList = new ArrayList<String>();
		termSequenceList = new ArrayList<TermSequence>(2);
		parse(query);
		terms = termList.toArray(new String[termList.size()]);
	}

	private final int checkTerm(final String term) {
		Integer pos = termMap.get(term);
		if (pos != null)
			return pos;
		pos = termList.size();
		termMap.put(term, pos);
		termList.add(term);
		return pos;
	}

	private final void parse(final TermQuery query) {
		Term term = query.getTerm();
		if (!field.equals(term.field()))
			return;
		int pos = checkTerm(term.text());
		termQuerySet.add(pos);
	}

	private final static class TermSequence {

		private final int slop;
		private final int[] terms;

		private TermSequence(final List<Integer> termPosSequence, final int slop) {
			int i = 0;
			terms = new int[termPosSequence.size()];
			for (Integer termPos : termPosSequence)
				terms[i++] = termPos;
			this.slop = slop;
		}

	}

	private final void parse(final PhraseQuery query) {
		Term[] terms = query.getTerms();
		if (terms == null)
			return;

		List<Integer> termPosSequence = new ArrayList<Integer>(terms.length);
		for (Term term : terms) {
			if (!field.equals(term.field()))
				continue;
			int pos = checkTerm(term.text());
			termPosSequence.add(pos);
			termPhraseSet.add(pos);
		}
		// Term sequences with one term are not phrase queries
		if (termPosSequence.size() <= 1)
			return;
		termSequenceList
				.add(new TermSequence(termPosSequence, query.getSlop()));
	}

	private final void parse(final BooleanQuery query) {
		BooleanClause[] clauses = query.getClauses();
		if (clauses == null)
			return;
		for (BooleanClause clause : clauses) {
			switch (clause.getOccur()) {
			case MUST:
			case SHOULD:
				parse(clause.getQuery());
				break;
			default:
				break;
			}
		}
	}

	private final void parse(final Query query) {
		if (query == null)
			return;
		if (query instanceof BooleanQuery)
			parse((BooleanQuery) query);
		else if (query instanceof TermQuery)
			parse((TermQuery) query);
		else if (query instanceof PhraseQuery)
			parse((PhraseQuery) query);
	}

	private final void checkTermQueries(
			final Collection<SnippetVector> vectors, final long expiration) {
		if (termQuerySet.isEmpty())
			return;
		for (SnippetVector vector : vectors) {
			if (!vector.query) {
				if (termQuerySet.contains(vector.term))
					vector.query = true;
				if (expiration != 0)
					if (System.currentTimeMillis() > expiration)
						return;
			}
		}
	}

	private static class SequenceCollector {

		private enum Result {
			WRONG, CONTINUE, FULL
		};

		private final TermSequence termSequence;
		private final SnippetVector[] vectors;
		private int foundPos;
		private int nextPosition;
		private int nextTerm;

		private SequenceCollector(final TermSequence termSequence,
				final SnippetVector vector) {
			this.termSequence = termSequence;
			vectors = new SnippetVector[termSequence.terms.length];
			foundPos = 0;
			addVector(vector);
		}

		private final Result addVector(final SnippetVector vector) {
			vectors[foundPos++] = vector;
			nextPosition = vector.position + termSequence.slop + 1;
			if (foundPos == vectors.length)
				return Result.FULL;
			nextTerm = termSequence.terms[foundPos];
			return Result.CONTINUE;
		}

		private final Result collect(final SnippetVector vector) {
			if (vector.position > nextPosition)
				return Result.WRONG;
			if (vector.term != nextTerm)
				return Result.CONTINUE;
			if (addVector(vector) != Result.FULL)
				return Result.CONTINUE;
			for (SnippetVector v : vectors)
				v.query = true;
			return Result.FULL;
		}
	}

	private final void checkPhraseQueries(
			final Collection<SnippetVector> vectors, final long expiration) {
		if (termSequenceList.isEmpty())
			return;
		Set<SequenceCollector> collectors = new HashSet<SequenceCollector>();
		List<SequenceCollector> toRemove = new ArrayList<SequenceCollector>();
		for (SnippetVector vector : vectors) {
			if (!(termPhraseSet.contains(vector.term)))
				continue;
			for (TermSequence termSequence : termSequenceList) {
				if (termSequence.terms[0] == vector.term)
					collectors.add(new SequenceCollector(termSequence, vector));
			}
			for (SequenceCollector collector : collectors) {
				switch (collector.collect(vector)) {
				case CONTINUE:
					break;
				case WRONG:
				case FULL:
					toRemove.add(collector);
					break;
				}
			}
			if (!toRemove.isEmpty()) {
				collectors.removeAll(toRemove);
				toRemove.clear();
			}
			if (expiration != 0)
				if (System.currentTimeMillis() > expiration)
					return;
		}
	}

	final void checkQueries(final Collection<SnippetVector> vectors,
			final Timer parentTimer, final long expiration) {
		if (vectors == null)
			return;
		Timer t = new Timer(parentTimer, "checkTermQueries");
		checkTermQueries(vectors, expiration);
		t.end(null);
		t = new Timer(parentTimer, "checkPhraseQueries");
		checkPhraseQueries(vectors, expiration);
		t.end(null);
	}
}
