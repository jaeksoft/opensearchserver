/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class ScoreBufferCollector extends AbstractDocSetHitCollector implements
		ScoreInterface, DocSetHitCollectorInterface {

	protected float maxScore = 0;
	protected FloatBufferedArray scoreCollector;
	protected float[] scores;

	public ScoreBufferCollector(final DocSetHitCollector base) {
		super(base);
		scoreCollector = new FloatBufferedArray(base.getMaxDoc());
		scores = null;
	}

	@Override
	public void collectDoc(final int docId) throws IOException {
		parent.collectDoc(docId);
		float sc = base.score();
		if (sc > maxScore)
			maxScore = sc;
		scoreCollector.add(sc);
	}

	@Override
	public void endCollection() {
		parent.endCollection();
		scores = scoreCollector.getFinalArray();
	}

	@Override
	final public int getSize() {
		return scores == null ? 0 : scores.length;
	}

	@Override
	final public float getMaxScore() {
		return maxScore;
	}

	@Override
	final public void swap(final int a, final int b) {
		parent.swap(a, b);
		float s1 = scores[a];
		float s2 = scores[b];
		scores[a] = s2;
		scores[b] = s1;
	}

	@Override
	final public float[] getScores() {
		return scores;
	}

}
