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

public class JoinScoreDocCollector extends JoinDocCollector implements
		ScoreDocInterface {

	public final static JoinScoreDocCollector EMPTY = new JoinScoreDocCollector();

	private float maxScore;
	protected final float[] scores;

	public JoinScoreDocCollector() {
		super();
		maxScore = 0;
		scores = ScoreDocInterface.EMPTY_SCORES;
	}

	public JoinScoreDocCollector(ScoreDocInterface docs, int joinResultSize) {
		super(docs, joinResultSize);
		maxScore = docs.getMaxScore();
		this.scores = ArrayUtils.clone(docs.getScores());
	}

	private JoinScoreDocCollector(JoinScoreDocCollector src) {
		super(src);
		maxScore = src.getMaxScore();
		this.scores = ArrayUtils.clone(src.getScores());
	}

	@Override
	public DocIdInterface duplicate() {
		return new JoinScoreDocCollector(this);
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
