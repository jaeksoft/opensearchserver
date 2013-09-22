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

package com.jaeksoft.searchlib.analysis;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public abstract class TokenQueryFilter extends AbstractTermFilter {

	public final CompiledAnalyzer analyzer;
	public final String field;
	public final float boost;

	public TokenQueryFilter(final CompiledAnalyzer analyzer,
			final String field, final float boost, TokenStream input) {
		super(input);
		this.analyzer = analyzer;
		this.field = field;
		this.boost = boost;
	}

	public class TermQueryItem {

		public final String term;
		public final int start;
		public final int end;
		public List<TermQueryItem> children;
		public TermQueryItem parent;

		public TermQueryItem(final String term, final int start, final int end) {
			this.term = term;
			this.start = start;
			this.end = end;
			this.children = null;
			this.parent = null;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(term);
			sb.append(" (");
			sb.append(start);
			sb.append(',');
			sb.append(end);
			sb.append(')');
			if (children != null)
				sb.append(" father");
			if (parent != null)
				sb.append(" child");
			return sb.toString();
		}

		public final boolean includes(TermQueryItem next) {
			return next.start >= start && next.end <= end;
		}

		public final void addChild(TermQueryItem next) {
			if (children == null)
				children = new ArrayList<TermQueryItem>(0);
			children.add(next);
			next.parent = this;
		}

		public final Query getTermOrPhraseQuery() throws IOException {
			if (analyzer != null) {
				List<TokenTerm> tokenTerms = new ArrayList<TokenTerm>(1);
				analyzer.justTokenize(term, tokenTerms);
				if (tokenTerms.size() > 1) {
					PhraseQuery phraseQuery = new PhraseQuery();
					for (TokenTerm tokenTerm : tokenTerms)
						phraseQuery.add(new Term(field, tokenTerm.term));
					phraseQuery.setBoost(boost);
					phraseQuery.setSlop(0);
					return phraseQuery;
				}
			}
			TermQuery termQuery = new TermQuery(new Term(field, term));
			termQuery.setBoost(boost);
			return termQuery;
		}

		private final Query getChildBooleanQuery(final Occur occur)
				throws IOException {
			if (children.size() == 1)
				return children.get(0).getTermOrPhraseQuery();
			BooleanQuery booleanQuery = new BooleanQuery();
			for (TermQueryItem child : children)
				booleanQuery.add(child.getTermOrPhraseQuery(), occur);
			return booleanQuery;
		}

		public final Query getQuery(final Occur occur) throws IOException {
			if (children == null)
				return getTermOrPhraseQuery();
			BooleanQuery booleanQuery = new BooleanQuery();
			booleanQuery.add(getTermOrPhraseQuery(), Occur.SHOULD);
			booleanQuery.add(getChildBooleanQuery(occur), Occur.SHOULD);
			return booleanQuery;
		}

		public void includeChilds() {
			if (children == null)
				return;
			TermQueryFilter.includeChilds(children);
			for (TermQueryItem child : children)
				child.includeChilds();
		}
	}

	public static class TermQueryFilter extends TokenQueryFilter implements
			IntComparator, Swapper {

		public final List<TermQueryItem> termQueryItems;

		public TermQueryFilter(final CompiledAnalyzer analyzer,
				final String field, final float boost, TokenStream input) {
			super(analyzer, field, boost, input);
			termQueryItems = new ArrayList<TermQueryItem>();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			termQueryItems.add(new TermQueryItem(termAtt.toString(), offsetAtt
					.startOffset(), offsetAtt.endOffset()));
			return true;
		}

		@Override
		public void swap(int a, int b) {
			TermQueryItem tqfa = termQueryItems.get(a);
			TermQueryItem tqfb = termQueryItems.get(b);
			termQueryItems.set(a, tqfb);
			termQueryItems.set(b, tqfa);
		}

		@Override
		public int compare(int a, int b) {
			return compareInt(a, b);
		}

		@Override
		public int compare(Integer a, Integer b) {
			return compareInt(a, b);
		}

		public void sortByOffset() {
			Arrays.quickSort(0, termQueryItems.size(), this, this);
		}

		public int compareInt(int k1, int k2) {
			TermQueryItem item1 = termQueryItems.get(k1);
			TermQueryItem item2 = termQueryItems.get(k2);
			if (item2.start == item1.start)
				return item2.end - item1.end;
			return item1.start - item2.start;
		}

		public final static void includeChilds(
				List<TermQueryItem> termQueryItems) {
			Iterator<TermQueryItem> iterator = termQueryItems.iterator();
			if (!iterator.hasNext())
				return;
			TermQueryItem current = iterator.next();
			while (iterator.hasNext()) {
				TermQueryItem next = iterator.next();
				if (current.includes(next)) {
					current.addChild(next);
					iterator.remove();
				} else
					current = next;
			}
		}
	}

	public static class BooleanQueryFilter extends TokenQueryFilter {

		public final BooleanQuery booleanQuery;
		public final Occur occur;

		public BooleanQueryFilter(BooleanQuery booleanQuery, Occur occur,
				String field, float boost, TokenStream input) {
			super(null, field, boost, input);
			this.booleanQuery = booleanQuery;
			this.occur = occur;
		}

		@Override
		public boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			TermQuery termQuery = new TermQuery(new Term(field,
					termAtt.toString()));
			termQuery.setBoost(boost);
			booleanQuery.add(termQuery, occur);
			return true;
		}
	}

	public static class PhraseQueryFilter extends TokenQueryFilter {

		public final PhraseQuery query;

		public PhraseQueryFilter(PhraseQuery query, String field, float boost,
				TokenStream input) {
			super(null, field, boost, input);
			this.query = query;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			query.add(new Term(field, termAtt.toString()));
			return true;
		}

	}
}
