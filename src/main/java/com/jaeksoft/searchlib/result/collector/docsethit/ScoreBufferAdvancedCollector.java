/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.scoring.AdvancedScoreItem;
import com.jaeksoft.searchlib.scoring.AdvancedScoreItemValue;

public class ScoreBufferAdvancedCollector extends ScoreBufferCollector {

	private final ScoreBufferCollector scoreBufferCollector;
	private final float scoreWeight;
	private final AdvancedScoreItemValue[] scoreItemValues;
	private int size;

	public ScoreBufferAdvancedCollector(final ReaderAbstract reader,
			final AbstractSearchRequest request,
			final DocSetHitBaseCollector base,
			final ScoreBufferCollector scoreBufferCollector,
			final DistanceCollector distanceCollector) throws IOException {
		super(base);
		this.scoreBufferCollector = scoreBufferCollector;

		AdvancedScore advancedScore = request.getAdvancedScore();
		AdvancedScoreItem[] scoreItems = advancedScore.getArray();
		scoreItemValues = new AdvancedScoreItemValue[scoreItems == null ? 0
				: scoreItems.length];
		int i = 0;
		if (scoreItems != null)
			for (AdvancedScoreItem scoreItem : scoreItems)
				scoreItemValues[i++] = new AdvancedScoreItemValue(request,
						reader, scoreItem, distanceCollector);
		this.scoreWeight = (float) advancedScore.getScoreWeight();
		size = 0;
	}

	private ScoreBufferAdvancedCollector(final DocSetHitBaseCollector base,
			final ScoreBufferAdvancedCollector src) {
		super(base, src);
		scoreBufferCollector = null;
		scoreWeight = src.scoreWeight;
		scoreItemValues = null;
		size = src.size;
	}

	@Override
	public ScoreBufferAdvancedCollector duplicate(
			final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new ScoreBufferAdvancedCollector((DocSetHitBaseCollector) base,
				this);
	}

	@Override
	final public void collectDoc(final int doc) throws IOException {
		parent.collectDoc(doc);
		for (AdvancedScoreItemValue scoreItemValue : scoreItemValues)
			scoreItemValue.collect(doc);
		size++;
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		for (AdvancedScoreItemValue scoreItemValue : scoreItemValues)
			scoreItemValue.endCollection();
		if (scoreWeight <= 0)
			endCollectionWithoutVSMScore();
		else
			endCollectionWithVSMScore(scoreBufferCollector.getScores());
		scores = scoreCollector.getFinalArray();
	}

	final private void endCollectionWithoutVSMScore() {
		for (int i = 0; i < size; i++) {
			float sc = 0;
			for (AdvancedScoreItemValue scoreItemValue : scoreItemValues)
				sc += scoreItemValue.finalArray[i];
			scoreCollector.add(sc);
		}
	}

	final private void endCollectionWithVSMScore(float[] scores) {
		for (int i = 0; i < size; i++) {
			float sc = scores[i] * scoreWeight;
			for (AdvancedScoreItemValue scoreItemValue : scoreItemValues)
				sc += scoreItemValue.finalArray[i];
			scoreCollector.add(sc);
		}
	}
}
