/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.function.expression;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.ValueSourceQuery;

public class ScoreFunctionQuery extends CustomScoreQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8153528644026354484L;

	private Query subQuery;

	private Expression expression;

	private class ScoreFunctionProvider extends CustomScoreProvider {

		public ScoreFunctionProvider(IndexReader reader) {
			super(reader);
		}

		@Override
		public float customScore(int doc, float subQueryScore, float valSrcScore) {
			return expression.getValue(subQueryScore, valSrcScore);
		}

		@Override
		public float customScore(int doc, float subQueryScore,
				float[] valSrcScores) {
			return expression.getValue(subQueryScore, valSrcScores);
		}
	}

	protected ScoreFunctionQuery(Query subQuery, Expression expression)
			throws SyntaxError {
		super(subQuery);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery valueSourceQuery, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQuery);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery[] valueSourceQueries, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQueries);
		this.subQuery = subQuery;
		this.expression = expression;
	}

	@Override
	protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) {
		return new ScoreFunctionProvider(reader);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("score(");
		if (subQuery != null)
			sb.append(subQuery.toString());
		sb.append(')');
		if (expression != null)
			sb.append(expression.toString());
		return sb.toString();
	}
}
