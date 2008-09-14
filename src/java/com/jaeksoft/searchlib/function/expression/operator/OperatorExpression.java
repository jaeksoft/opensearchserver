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

package com.jaeksoft.searchlib.function.expression.operator;

import com.jaeksoft.searchlib.function.expression.Expression;

public abstract class OperatorExpression extends Expression {

	protected OperatorExpression(int pos) {
		super(null);
		nextPos = ++pos;
	}

	@Override
	protected float getValue(float subQueryScore, float valSrcScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected float getValue(float subQueryScore, float[] valSrcScores) {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract float newValue(float value1, float value2);
}
