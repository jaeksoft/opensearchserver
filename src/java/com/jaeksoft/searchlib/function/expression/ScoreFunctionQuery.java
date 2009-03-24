/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.function.expression;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.ValueSourceQuery;

public class ScoreFunctionQuery extends CustomScoreQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3408889704609057463L;

	private Expression expression;

	protected ScoreFunctionQuery(Query subQuery, Expression expression)
			throws SyntaxError {
		super(subQuery);
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery valueSourceQuery, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQuery);
		this.expression = expression;
	}

	protected ScoreFunctionQuery(Query subQuery,
			ValueSourceQuery[] valueSourceQueries, Expression expression)
			throws SyntaxError {
		super(subQuery, valueSourceQueries);
		this.expression = expression;
	}

	@Override
	public float customScore(int doc, float subQueryScore, float valSrcScore) {
		return expression.getValue(subQueryScore, valSrcScore);
	}

	@Override
	public float customScore(int doc, float subQueryScore, float[] valSrcScores) {
		return expression.getValue(subQueryScore, valSrcScores);
	}

	@Override
	public String toString() {
		return expression.toString();
	}

}
