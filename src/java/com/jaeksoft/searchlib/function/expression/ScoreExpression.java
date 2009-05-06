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

public class ScoreExpression extends Expression {

	protected ScoreExpression(RootExpression root, int pos) {
		super(root);
		nextPos = pos + 5;
	}

	@Override
	protected float getValue(float subQueryScore, float valSrcScore) {
		return subQueryScore * valSrcScore;
	}

	@Override
	protected float getValue(float subQueryScore, float[] valSrcScores) {
		float v = subQueryScore;
		for (float f : valSrcScores)
			v *= f;
		return v;
	}

}
