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

	private final float maxScore;
	private final float[] scores;

	public JoinScoreDocCollector() {
		super();
		maxScore = 0;
		scores = ScoreDocInterface.EMPTY_SCORES;
	}

	public JoinScoreDocCollector(DocIdInterface docIdInterface,
			ScoreDocInterface scoreDocInterface, int joinResultSize) {
		super(docIdInterface, joinResultSize);
		maxScore = scoreDocInterface.getMaxScore();
		this.scores = ArrayUtils.clone(scoreDocInterface.getScores());
	}

	private JoinScoreDocCollector(JoinScoreDocCollector src) {
		super(src.joinResultSize, validSize(src.ids), src.maxDoc);
		scores = new float[ids.length];
		int i1 = 0;
		int i2 = 0;
		float msc = 0;
		for (int id : src.ids) {
			if (id != -1) {
				ids[i1] = id;
				float score = src.scores[i2];
				scores[i1] = score;
				if (score > msc)
					msc = score;
				foreignDocIdsArray[i1++] = ArrayUtils
						.clone(src.foreignDocIdsArray[i2]);
			}
			i2++;
		}
		maxScore = msc;
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
	final public void setForeignDocId(int pos, int joinResultPos,
			int foreignDocId, float foreignScore) {
		super.setForeignDocId(pos, joinResultPos, foreignDocId, foreignScore);
		float score = this.scores[pos] * foreignScore;
		this.scores[pos] = score;
	}

	@Override
	public void swap(int pos1, int pos2) {
		super.swap(pos1, pos2);
		float score = scores[pos1];
		scores[pos1] = scores[pos2];
		scores[pos2] = score;
	}

}
