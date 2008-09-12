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

import com.jaeksoft.searchlib.function.token.QuoteToken;

public class VariableExpression extends Expression {

	private String var;

	protected VariableExpression(char[] chars, int pos) {
		QuoteToken token = new QuoteToken(chars, pos);
		var = token.word;
		nextPos = pos + token.size + 2;
	}

	@Override
	protected float getValue(int docId, float subQueryScore, float valSrcScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('"');
		sb.append(var);
		sb.append('"');
		return sb.toString();
	}

}
