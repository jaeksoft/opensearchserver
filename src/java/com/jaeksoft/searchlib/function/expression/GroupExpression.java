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

import com.jaeksoft.searchlib.function.expression.operator.DivideExpression;
import com.jaeksoft.searchlib.function.expression.operator.MinusExpression;
import com.jaeksoft.searchlib.function.expression.operator.MultiplyExpression;
import com.jaeksoft.searchlib.function.expression.operator.OperatorExpression;
import com.jaeksoft.searchlib.function.expression.operator.PlusExpression;

public class GroupExpression extends Expression {

	protected ArrayList<Expression> expressions;

	protected GroupExpression(RootExpression root, char[] chars, int pos)
			throws SyntaxError {
		super(root);
		expressions = new ArrayList<Expression>();
		while (pos < chars.length) {
			Expression exp = nextExpression(chars, pos);
			if (exp == null) {
				pos++;
				break;
			}
			expressions.add(exp);
			pos = exp.nextPos;
		}
		nextPos = pos;
	}

	private Expression nextExpression(char[] chars, int pos) throws SyntaxError {
		if (pos >= chars.length)
			return null;
		char ch = chars[pos];
		if (ch == '(')
			return new GroupExpression(root, chars, pos + 1);
		if (Character.isDigit(ch))
			return new FloatExpression(chars, pos);
		if (Character.isLetter(ch))
			return new FunctionExpression(root, chars, pos);
		if (ch == '+')
			return new PlusExpression(pos);
		if (ch == '*')
			return new MultiplyExpression(pos);
		if (ch == '/')
			return new DivideExpression(pos);
		if (ch == '-')
			return new MinusExpression(pos);
		if (ch == ')')
			return null;
		throw new SyntaxError("Syntax error", pos);
	}

	@Override
	protected float getValue(int docId, float subQueryScore, float valSrcScore) {
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

	protected float getValue(int docId, float subQueryScore,
			float[] valSrcScores) {
		float value = 0;
		OperatorExpression operator = new PlusExpression(0);
		for (Expression expression : expressions) {
			if (expression instanceof OperatorExpression)
				operator = (OperatorExpression) expression;
			else {
				value = operator.newValue(value, expression.getValue(docId,
						subQueryScore, valSrcScores));
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
