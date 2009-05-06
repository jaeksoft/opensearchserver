/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.function.expression;

import com.jaeksoft.searchlib.function.token.LetterOrDigitToken;

public class FunctionExpression extends Expression {

	private FunctionValueSource functionValueSource;

	protected FunctionExpression(RootExpression root, char[] chars, int pos)
			throws SyntaxError {
		super(root);
		char[] addchars = { '.', '_', '-' };
		LetterOrDigitToken token = new LetterOrDigitToken(chars, pos, addchars);
		String func = token.word;
		pos += token.size;
		if (pos >= chars.length)
			throw new SyntaxError("Parenthesis missing", chars, pos);
		if (chars[pos++] != '(')
			throw new SyntaxError("Parenthesis missing", chars, pos);
		token = new LetterOrDigitToken(chars, pos, addchars);
		String field = token.word;
		functionValueSource = root.functionValueSource(func, field);
		pos += token.size;
		if (pos >= chars.length)
			throw new SyntaxError("Parenthesis missing", chars, pos);
		if (chars[pos] != ')')
			throw new SyntaxError("Parenthesis missing", chars, pos);
		pos++;
		nextPos = pos;
	}

	@Override
	protected float getValue(float subQueryScore, float valSrcScore) {
		return valSrcScore;
	}

	@Override
	protected float getValue(float subQueryScore, float[] valSrcScores) {
		if (valSrcScores.length == 0)
			return 0;
		return valSrcScores[functionValueSource.pos];
	}

	@Override
	public String toString() {
		return functionValueSource.toString();
	}
}
