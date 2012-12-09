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

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

public class ScoreDocCollector extends DocIdCollector implements
		ScoreDocInterface {

	protected float maxScore = 0;
	protected float[] scores;

	public ScoreDocCollector(int maxDoc, int numFound) {
		super(maxDoc, numFound);
		scores = new float[numFound];
	}

	protected ScoreDocCollector(ScoreDocCollector src) {
		super(src);
		this.maxScore = src.maxScore;
		this.scores = ArrayUtils.clone(src.scores);
	}

	@Override
	public DocIdInterface duplicate() {
		return new ScoreDocCollector(this);
	}

	@Override
	final public void collect(int docId) throws IOException {
		float sc = scorer.score();
		if (sc > maxScore)
			maxScore = sc;
		ids[currentPos] = docId;
		scores[currentPos++] = sc;
	}

	@Override
	final public float getMaxScore() {
		return maxScore;
	}

	@Override
	final public void swap(int i, int j) {
		int id = ids[i];
		float score = scores[i];
		ids[i] = ids[j];
		scores[i] = scores[j];
		ids[j] = id;
		scores[j] = score;
	}

	@Override
	final public float[] getScores() {
		return scores;
	}

}
