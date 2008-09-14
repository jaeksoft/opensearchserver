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

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

public abstract class Expression {

	protected RootExpression root;

	protected int nextPos;

	protected Expression(RootExpression root) {
		if (root == null && this instanceof RootExpression)
			this.root = (RootExpression) this;
		else
			this.root = root;
		this.nextPos = 0;
	}

	protected abstract float getValue(int docId, float subQueryScore,
			float valSrcScore);

	protected abstract float getValue(int docId, float subQueryScore,
			float[] valSrcScores);

	static public ScoreFunctionQuery getQuery(Query subQuery, String exp)
			throws SyntaxError {
		exp = exp.trim().replaceAll("\\s+", "");
		Expression expression = new RootExpression(exp.toCharArray(), 0);
		return new ScoreFunctionQuery(subQuery, expression);
	}

	public static void main(String[] argv) {
		String exp = " 10000 / ( 1 * rord(creationDate) + 10000 ) ";
		try {
			System.out.println(getQuery(new BooleanQuery(), exp));
		} catch (SyntaxError e) {
			System.err.println(e.showError(exp));
		}
	}

}
