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

import java.util.ArrayList;

import com.jaeksoft.searchlib.function.SyntaxError;
import com.jaeksoft.searchlib.function.expression.operator.OperatorExpression;
import com.jaeksoft.searchlib.function.expression.operator.PlusExpression;

public class GroupExpression extends Expression {

	protected ArrayList<Expression> expressions;

	public GroupExpression(char[] chars, int pos) throws SyntaxError {
		expressions = new ArrayList<Expression>();
		while (pos < chars.length) {
			Expression exp = Expression.nextExpression(chars, pos);
			if (exp == null)
				break;
			expressions.add(exp);
			pos = exp.nextPos;
		}
		nextPos = pos;
	}

	@Override
	public float getValue(int docId, float subQueryScore, float valSrcScore) {
		float value = 0;
		OperatorExpression operator = new PlusExpression(0);
		for (Expression expression : expressions) {
			if (expression instanceof OperatorExpression)
				operator = (OperatorExpression) expression;
			else {
				value = operator.newValue(value, expression.getValue(docId,
						subQueryScore, valSrcScore));
			}
		}
		return value;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (Expression expression : expressions)
			sb.append(expression.toString());
		sb.append(')');
		return sb.toString();
	}
}
