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

package com.jaeksoft.searchlib.result.collector.docsethit;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;
import com.jaeksoft.searchlib.util.array.FloatBufferedArrayFactory;
import com.jaeksoft.searchlib.util.array.FloatBufferedArrayInterface;

public class ScoreBufferCollector
		extends
		AbstractExtendsCollector<DocSetHitCollectorInterface, DocSetHitBaseCollector>
		implements ScoreInterface, DocSetHitCollectorInterface {

	final protected FloatBufferedArrayInterface scoreCollector;
	protected float maxScore = 0;
	protected float[] scores;

	public ScoreBufferCollector(final DocSetHitBaseCollector base) {
		super(base);
		scoreCollector = FloatBufferedArrayFactory.INSTANCE.newInstance(base
				.getMaxDoc());
		scores = null;
	}

	protected ScoreBufferCollector(final DocSetHitBaseCollector base,
			final ScoreBufferCollector src) {
		super(base);
		scoreCollector = null;
		scores = src.scores == null ? null : ArrayUtils.clone(src.scores);
		maxScore = src.maxScore;
	}

	@Override
	public CollectorInterface duplicate(final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new ScoreBufferCollector((DocSetHitBaseCollector) base, this);
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
	final public float getMaxScore() {
		return maxScore;
	}

	@Override
	final public void doSwap(final int a, final int b) {
		parent.doSwap(a, b);
		float s1 = scores[a];
		float s2 = scores[b];
		scores[a] = s2;
		scores[b] = s1;
	}

	@Override
	final public float[] getScores() {
		return scores;
	}

	@Override
	final public int getSize() {
		if (scores == null)
			return 0;
		return scores.length;
	}

}
