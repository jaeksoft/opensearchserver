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

package com.jaeksoft.searchlib.function;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreQuery;

import com.jaeksoft.searchlib.function.expression.GroupExpression;

public class ScoreFunctionQuery extends CustomScoreQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3408889704609057463L;

	private GroupExpression expression;

	public ScoreFunctionQuery(Query subQuery, String scoreFunction)
			throws SyntaxError {
		super(subQuery);
		// Remove all white-spaces
		scoreFunction = scoreFunction.trim().replaceAll("\\s+", "");

		int pos = 0;

		char[] chars = scoreFunction.toCharArray();

		expression = new GroupExpression(chars, pos);
	}

	public float customScore(int docId, float subQueryScore, float valSrcScore) {
		return expression.getValue(docId, subQueryScore, valSrcScore);
	}

	public String toString() {
		return expression.toString();
	}

	public static void main(String[] argv) {
		String exp = " 10000 / ( 1 * rord( \"creationDate\") + 10000 ) ";
		try {
			System.out.println(new ScoreFunctionQuery(new BooleanQuery(), exp));
		} catch (SyntaxError e) {
			System.err.println(e.showError(exp));
		}
	}
}
