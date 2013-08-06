/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.learning;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LearnerResultItem {

	public final double score;
	public int rank;
	public final String target;

	public LearnerResultItem(double score, int rank, String target) {
		this.score = score;
		this.rank = rank;
		this.target = target;
	}

	public double getScore() {
		return score;
	}

	public String getTarget() {
		return target;
	}

	public int getRank() {
		return rank;
	}

	public final static LearnerResultItem[] sortArray(
			List<LearnerResultItem> list) {
		if (list == null)
			return null;
		LearnerResultItem[] array = list.toArray(new LearnerResultItem[list
				.size()]);
		Arrays.sort(array, new ScoreDesc());
		return array;
	}

	public final static class ScoreDesc implements
			Comparator<LearnerResultItem> {

		@Override
		public int compare(LearnerResultItem i1, LearnerResultItem i2) {
			return Double.compare(i2.score, i1.score);
		}

	}

	private final static void computeRank(LearnerResultItem[] result) {
		int rank = 0;
		double score = -1;
		for (LearnerResultItem item : result) {
			item.rank = rank;
			if (item.score == score)
				continue;
			rank++;
			score = item.score;
		}
	}

	private final static LearnerResultItem[] limitMaxRank(
			LearnerResultItem[] result, int maxRank) {
		int pos = 0;
		for (LearnerResultItem item : result)
			if (item.rank == maxRank)
				break;
			else
				pos++;
		System.out.println("POS " + pos);
		if (pos == result.length)
			return result;
		return Arrays.copyOf(result, pos);
	}

	public final static LearnerResultItem[] maxRank(LearnerResultItem[] result,
			int maxRank) {
		if (result == null)
			return null;
		if (result.length == 0)
			return null;
		if (result[0].rank == -1)
			computeRank(result);
		if (maxRank != 0)
			result = limitMaxRank(result, maxRank);
		return result;
	}
}
