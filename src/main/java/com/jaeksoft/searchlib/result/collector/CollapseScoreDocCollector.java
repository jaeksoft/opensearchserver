/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result.collector;

import org.apache.commons.lang3.ArrayUtils;

public class CollapseScoreDocCollector extends CollapseDocIdCollector implements
		CollapseDocInterface, ScoreDocInterface {

	private float maxScore;
	protected final float[] sourceScores;
	protected final float[] scores;

	public CollapseScoreDocCollector(ScoreDocInterface sourceCollector,
			int size, int collapseMax) {
		super(sourceCollector, size, collapseMax);
		this.sourceScores = sourceCollector.getScores();
		scores = new float[size];
		maxScore = 0;
	}

	protected CollapseScoreDocCollector(CollapseScoreDocCollector src) {
		super(src);
		this.sourceScores = src.sourceScores;
		this.maxScore = src.maxScore;
		this.scores = ArrayUtils.clone(src.scores);
	}

	@Override
	public DocIdInterface duplicate() {
		return new CollapseScoreDocCollector(this);
	}

	@Override
	public int collectDoc(int sourcePos) {
		int pos = super.collectDoc(sourcePos);
		float sc = sourceScores[sourcePos];
		scores[pos] = sc;
		if (maxScore < sc)
			maxScore = sc;
		return pos;
	}

	@Override
	public float[] getScores() {
		return scores;
	}

	@Override
	public float getMaxScore() {
		return maxScore;
	}

	@Override
	public void swap(int pos1, int pos2) {
		super.swap(pos1, pos2);
		float score = scores[pos1];
		scores[pos1] = scores[pos2];
		scores[pos2] = score;
	}

}
