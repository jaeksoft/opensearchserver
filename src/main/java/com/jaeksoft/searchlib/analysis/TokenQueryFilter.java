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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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

	public int termCount;

	public TokenQueryFilter(TokenStream input) {
		super(input);
		this.termCount = 0;
	}

	public static class TermQueryItem {

		public final String term;
		public final int start;
		public final int end;
		public List<TermQueryItem> children;
		public TermQueryItem parent;

		public TermQueryItem(String term, int start, int end) {
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

		public final TermQuery getTermQuery(String field, float boost) {
			TermQuery termQuery = new TermQuery(new Term(field, term));
			termQuery.setBoost(boost);
			return termQuery;
		}

		private final Query getChildBooleanQuery(String field, float boost,
				Occur occur) {
			if (children.size() == 1)
				return getTermQuery(field, boost);
			BooleanQuery booleanQuery = new BooleanQuery();
			for (TermQueryItem child : children)
				booleanQuery.add(child.getTermQuery(field, boost), occur);
			return booleanQuery;
		}

		public final Query getQuery(String field, float boost, Occur occur) {
			if (children == null)
				return getTermQuery(field, boost);
			BooleanQuery booleanQuery = new BooleanQuery();
			booleanQuery.add(getTermQuery(field, boost), Occur.SHOULD);
			booleanQuery.add(getChildBooleanQuery(field, boost, occur),
					Occur.SHOULD);
			return booleanQuery;
		}
	}

	public static class TermQueryFilter extends TokenQueryFilter implements
			Comparator<TermQueryItem> {

		public final List<TermQueryItem> termQueryItems;

		public TermQueryFilter(TokenStream input) {
			super(input);
			termQueryItems = new ArrayList<TermQueryItem>();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			termQueryItems.add(new TermQueryItem(termAtt.toString(), offsetAtt
					.startOffset(), offsetAtt.endOffset()));
			termCount++;
			return true;
		}

		@Override
		public int compare(TermQueryItem item1, TermQueryItem item2) {
			if (item1.start == item2.start)
				return item2.end - item1.end;
			return item1.end - item1.start;
		}

	}

	public static class BooleanQueryFilter extends TokenQueryFilter {

		public final BooleanQuery booleanQuery;
		private final String field;
		private final Occur occur;
		private final float boost;

		public BooleanQueryFilter(BooleanQuery booleanQuery, Occur occur,
				String field, float boost, TokenStream input) {
			super(input);
			this.field = field;
			this.booleanQuery = booleanQuery;
			this.occur = occur;
			this.boost = boost;

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

		private final String field;

		public final PhraseQuery query;

		public PhraseQueryFilter(PhraseQuery query, String field, float boost,
				TokenStream input) {
			super(input);
			this.field = field;
			this.query = query;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			query.add(new Term(field, termAtt.toString()));
			termCount++;
			return true;
		}

	}
}
