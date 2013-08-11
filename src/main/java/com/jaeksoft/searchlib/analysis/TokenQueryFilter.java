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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public abstract class TokenQueryFilter extends AbstractTermFilter {

	protected final String field;

	public int termCount;

	public TokenQueryFilter(String field, TokenStream input) {
		super(input);
		this.field = field;
		this.termCount = 0;
	}

	public static class BooleanQueryFilter extends TokenQueryFilter {

		public final BooleanQuery query;

		private final Occur occur;

		private final float boost;

		public BooleanQueryFilter(BooleanQuery query, Occur occur,
				String field, float boost, TokenStream input) {
			super(field, input);
			this.query = query;
			this.occur = occur;
			this.boost = boost;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			if (!input.incrementToken())
				return false;
			TermQuery tq = new TermQuery(new Term(field, termAtt.toString()));
			tq.setBoost(boost);
			query.add(tq, occur);
			termCount++;
			return true;
		}
	}

	public static class PhraseQueryFilter extends TokenQueryFilter {

		public final PhraseQuery query;

		public PhraseQueryFilter(PhraseQuery query, String field, float boost,
				TokenStream input) {
			super(field, input);
			this.query = query;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			if (!input.incrementToken())
				return false;
			query.add(new Term(field, termAtt.toString()));
			termCount++;
			return true;
		}

	}
}
