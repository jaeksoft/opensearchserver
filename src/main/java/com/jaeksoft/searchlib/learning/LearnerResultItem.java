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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class LearnerResultItem {

	public double score;
	public int rank;
	public final String target;

	public String name;

	public Double highestScore;

	public int count;

	public final TreeMap<String, ArrayList<String>> customs;

	public LearnerResultItem() {
		score = 0;
		rank = 0;
		target = null;
		name = null;
		highestScore = null;
		count = 0;
		customs = null;
	}

	public LearnerResultItem(double score, int rank, String target,
			String name, int count, TreeMap<String, ArrayList<String>> customs) {
		this.score = score;
		this.rank = rank;
		this.target = target;
		this.name = name;
		this.count = count;
		this.customs = customs;
		highestScore = null;
	}

	public LearnerResultItem(LearnerResultItem result,
			TreeMap<String, ArrayList<String>> customs) {
		this(result.score, result.rank, result.target, result.name,
				result.count, customs);
	}

	public double getScore() {
		return score;
	}

	public String getTarget() {
		return target;
	}

	public String getName() {
		return name;
	}

	public int getRank() {
		return rank;
	}

	public int getCount() {
		return count;
	}

	public final static LearnerResultItem[] sortArray(
			List<LearnerResultItem> list) {
		if (list == null)
			return null;
		LearnerResultItem[] array = list.toArray(new LearnerResultItem[list
				.size()]);
		Arrays.sort(array, new ScoreDescComparator());
		return array;
	}

	public final static class ScoreDescComparator implements
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

	public final void addScoreInstance(final double score, final int count,
			final String name) {
		this.score += score;
		this.count += count;
		if (highestScore == null || score > highestScore) {
			highestScore = score;
			if (name != null)
				this.name = name;
		}
	}

}
