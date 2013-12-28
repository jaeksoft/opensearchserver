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
import com.jaeksoft.searchlib.util.StringUtils;

class SnippetQueries {

	private final String field;
	private final Map<String, Integer> termMap;
	private final List<String> termList;
	private final Set<Integer> termQuerySet;
	private final Set<Integer> termPhraseSet;
	private final Map<String, TermSequence> termSequenceMap;
	final String[] terms;

	SnippetQueries(final Query query, final String field) {
		this.field = field;
		termQuerySet = new TreeSet<Integer>();
		termPhraseSet = new TreeSet<Integer>();
		termMap = new TreeMap<String, Integer>();
		termList = new ArrayList<String>();
		termSequenceMap = new TreeMap<String, TermSequence>();
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

		private final Set<Integer> slopSet;
		private final int[] terms;

		private TermSequence(final List<Integer> termPosSequence) {
			slopSet = new TreeSet<Integer>();
			int i = 0;
			terms = new int[termPosSequence.size()];
			for (Integer termPos : termPosSequence)
				terms[i++] = termPos;
		}

		private final void addSlop(int slop) {
			slopSet.add(slop);
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
		if (termPosSequence.size() == 0)
			return;
		String termPosSeq = StringUtils.join(termPosSequence, '|');
		TermSequence termSequence = termSequenceMap.get(termPosSeq);
		if (termSequence == null) {
			termSequence = new TermSequence(termPosSequence);
			termSequenceMap.put(termPosSeq, termSequence);
		}
		termSequence.addSlop(query.getSlop());
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

	private final void checkTermQueries(final Collection<SnippetVector> vectors) {
		if (termQuerySet.isEmpty())
			return;
		for (SnippetVector vector : vectors)
			if (!vector.query)
				if (termQuerySet.contains(vector.term))
					vector.query = true;
	}

	private static class SequenceCollector {

		private final TermSequence termSequence;
		private final SnippetVector[] vectors;
		private int foundPos;

		private SequenceCollector(TermSequence termSequence) {
			this.termSequence = termSequence;
			vectors = new SnippetVector[termSequence.terms.length];
			foundPos = 0;
		}

		private final void collect(SnippetVector vector) {
			if (isFull())
				return;
			if (vector.term != termSequence.terms[foundPos])
				return;
			vectors[foundPos++] = vector;
		}

		private final boolean isFull() {
			return foundPos == termSequence.terms.length;
		}

		private final boolean checkSuccess() {
			if (!isFull())
				return false;
			int maxSlop = 0;
			SnippetVector previous = null;
			for (SnippetVector current : vectors) {
				if (previous != null) {
					int newSlop = current.position - previous.position;
					if (newSlop > maxSlop)
						maxSlop = newSlop;
				}
				previous = current;
			}
			boolean success = false;
			for (Integer slop : termSequence.slopSet) {
				if (maxSlop <= slop) {
					success = true;
					break;
				}
			}
			if (!success)
				return false;
			for (SnippetVector vector : vectors)
				vector.query = true;
			return true;
		}
	}

	private final void checkPhraseQueries(
			final Collection<SnippetVector> vectors) {
		if (termSequenceMap.isEmpty())
			return;
		Collection<TermSequence> termSequences = termSequenceMap.values();
		Set<SequenceCollector> collectors = new HashSet<SequenceCollector>();
		List<SequenceCollector> toRemove = new ArrayList<SequenceCollector>();
		for (SnippetVector vector : vectors) {
			if (!(termPhraseSet.contains(vector.term)))
				continue;
			for (TermSequence termSequence : termSequences)
				if (termSequence.terms[0] == vector.term)
					collectors.add(new SequenceCollector(termSequence));
			for (SequenceCollector collector : collectors) {
				collector.collect(vector);
				if (collector.checkSuccess())
					toRemove.add(collector);
			}
			if (!toRemove.isEmpty()) {
				collectors.removeAll(toRemove);
				toRemove.clear();
			}
		}
	}

	final void checkQueries(final Collection<SnippetVector> vectors) {
		if (vectors == null)
			return;
		checkTermQueries(vectors);
		checkPhraseQueries(vectors);
	}
}
