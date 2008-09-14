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

import com.jaeksoft.searchlib.function.token.LetterOrDigitToken;

public class FunctionExpression extends Expression {

	private FunctionValueSource functionValueSource;

	protected FunctionExpression(RootExpression root, char[] chars, int pos)
			throws SyntaxError {
		super(root);
		LetterOrDigitToken token = new LetterOrDigitToken(chars, pos);
		String func = token.word;
		pos += token.size;
		if (pos >= chars.length)
			throw new SyntaxError("Parenthesis missing", pos);
		if (chars[pos++] != '(')
			throw new SyntaxError("Parenthesis missing", pos);
		token = new LetterOrDigitToken(chars, pos);
		String field = token.word;
		functionValueSource = root.functionValueSource(func, field);
		pos += token.size;
		if (pos >= chars.length)
			throw new SyntaxError("Parenthesis missing", pos);
		if (chars[pos++] != ')')
			throw new SyntaxError("Parenthesis missing", pos);
		nextPos = pos;
	}

	@Override
	protected float getValue(int docId, float subQueryScore, float valSrcScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected float getValue(int docId, float subQueryScore,
			float[] valSrcScores) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString() {
		return functionValueSource.toString();
	}
}
