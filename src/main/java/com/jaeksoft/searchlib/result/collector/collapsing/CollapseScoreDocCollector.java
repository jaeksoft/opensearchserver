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

package com.jaeksoft.searchlib.result.collector.collapsing;

import org.apache.commons.lang.ArrayUtils;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;
import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class CollapseScoreDocCollector
		extends
		AbstractExtendsCollector<CollapseCollectorInterface, CollapseBaseCollector>
		implements CollapseCollectorInterface, ScoreInterface {

	final private float[] sourceScores;
	final private FloatBufferedArray scoreCollector;
	final private float maxScore;

	private int currentPos;
	private float[] scores;

	public CollapseScoreDocCollector(final CollapseBaseCollector base,
			final ScoreInterface scoreInterface) {
		super(base);
		this.sourceScores = scoreInterface.getScores();
		this.scoreCollector = new FloatBufferedArray(scoreInterface.getSize());
		this.maxScore = 0;
		this.currentPos = 0;
		this.scores = null;
	}

	private CollapseScoreDocCollector(final CollapseBaseCollector base,
			final CollapseScoreDocCollector src) {
		super(base);
		this.sourceScores = null;
		this.scoreCollector = null;
		this.maxScore = src.maxScore;
		this.scores = ArrayUtils.clone(src.scores);
		this.currentPos = src.currentPos;

	}

	@Override
	final public CollapseScoreDocCollector duplicate(
			final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new CollapseScoreDocCollector((CollapseBaseCollector) base, this);
	}

	@Override
	final public int collectDoc(final int sourcePos) {
		int pos = parent.collectDoc(sourcePos);
		if (pos != currentPos)
			throw new RuntimeException("Internal position issue: " + pos
					+ " - " + currentPos);
		currentPos++;
		float sc = sourceScores[sourcePos];
		scoreCollector.add(sc);
		return pos;
	}

	@Override
	final public void collectCollapsedDoc(final int sourcePos,
			final int collapsePos) {
		parent.collectCollapsedDoc(sourcePos, collapsePos);
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		scores = scoreCollector.getFinalArray();
	}

	@Override
	final public float[] getScores() {
		return scores;
	}

	@Override
	final public float getMaxScore() {
		return maxScore;
	}

	@Override
	final public void doSwap(final int pos1, final int pos2) {
		parent.doSwap(pos1, pos2);
		float score = scores[pos1];
		scores[pos1] = scores[pos2];
		scores[pos2] = score;
	}

	@Override
	final public int getSize() {
		return currentPos;
	}

}
