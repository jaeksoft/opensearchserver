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

package com.jaeksoft.searchlib.result.collector.join;

import org.apache.commons.lang3.ArrayUtils;

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.JoinScoreInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;

public class JoinScoreCollector extends
		AbstractExtendsCollector<JoinCollectorInterface, JoinDocCollector>
		implements JoinCollectorInterface, JoinScoreInterface, ScoreInterface {

	private final float srcMaxScore;
	private final float[] srcScores;
	private final float[][] foreignScoresArray;

	private final static float[][] EMPTY = new float[0][0];

	JoinScoreCollector(JoinDocCollector base) {
		super(base);
		srcMaxScore = 0;
		srcScores = ScoreInterface.EMPTY_SCORES;
		foreignScoresArray = EMPTY;
	}

	JoinScoreCollector(JoinDocCollector base, ScoreInterface scoreDocs) {
		super(base);
		srcMaxScore = scoreDocs.getMaxScore();
		this.srcScores = ArrayUtils.clone(scoreDocs.getScores());
		this.foreignScoresArray = new float[srcScores.length][];
		if (scoreDocs instanceof JoinScoreCollector)
			((JoinScoreCollector) scoreDocs).copyForeignScoresArray(this);
	}

	private void copyForeignScoresArray(
			final JoinScoreCollector joinScoreCollector) {
		if (foreignScoresArray == null)
			return;
		int i = 0;
		for (float[] scores : foreignScoresArray)
			joinScoreCollector.foreignScoresArray[i++] = scores;
	}

	/**
	 * Copy only the valid item (other than -1)
	 * 
	 * @param src
	 */
	private JoinScoreCollector(final JoinDocCollector base,
			final JoinScoreCollector src) {
		super(base);
		this.foreignScoresArray = new float[base.srcIds.length][];
		this.srcScores = new float[base.srcIds.length];
		int i1 = 0;
		int i2 = 0;
		float msc = 0;
		for (int id : src.base.srcIds) {
			if (id != -1) {
				this.foreignScoresArray[i1] = ArrayUtils
						.clone(src.foreignScoresArray[i2]);
				float s = src.srcScores[i2];
				this.srcScores[i1++] = s;
				if (s > msc)
					msc = s;
			}
			i2++;
		}
		this.srcMaxScore = msc;
	}

	@Override
	public JoinScoreCollector duplicate(final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new JoinScoreCollector((JoinDocCollector) base, this);
	}

	@Override
	final public void doSetForeignDoc(final int pos, final int joinResultPos,
			final int foreignDocId, final float foreignScore) {
		parent.doSetForeignDoc(pos, joinResultPos, foreignDocId, foreignScore);
		float[] foreignScores = foreignScoresArray[pos];
		if (foreignScores == null) {
			foreignScores = new float[base.joinResultSize];
			foreignScoresArray[pos] = foreignScores;
		}
		foreignScores[joinResultPos] = foreignScore;
	}

	final public static void swap(final float[][] foreignScoresArray,
			final int pos1, final int pos2) {
		float[] foreignScores = foreignScoresArray[pos1];
		foreignScoresArray[pos1] = foreignScoresArray[pos2];
		foreignScoresArray[pos2] = foreignScores;
	}

	@Override
	final public void doSwap(final int pos1, final int pos2) {
		parent.doSwap(pos1, pos2);
		float s1 = srcScores[pos1];
		float s2 = srcScores[pos2];
		srcScores[pos1] = s2;
		srcScores[pos2] = s1;
		swap(foreignScoresArray, pos1, pos2);
	}

	@Override
	final public ReaderAbstract[] getForeignReaders() {
		return base.getForeignReaders();
	}

	@Override
	public int getSize() {
		return srcScores.length;
	}

	@Override
	public float getForeignScore(int pos, int joinPosition) {
		return foreignScoresArray[pos][joinPosition];
	}

	@Override
	public float[][] getForeignDocScoreArray() {
		return foreignScoresArray;
	}

	@Override
	public float getMaxScore() {
		return srcMaxScore;
	}

	@Override
	public float[] getScores() {
		return srcScores;
	}

}
