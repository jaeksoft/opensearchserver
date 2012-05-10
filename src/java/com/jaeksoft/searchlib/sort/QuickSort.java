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

package com.jaeksoft.searchlib.sort;

import java.util.Random;

import com.jaeksoft.searchlib.result.ResultScoreDoc;

public class QuickSort {

	private final Random random;

	private final SorterAbstract sorter;

	public QuickSort(SorterAbstract sorter) {
		random = new Random();
		this.sorter = sorter;
	}

	private final void swap(ResultScoreDoc[] array, int i, int j) {
		ResultScoreDoc tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;
	}

	private final int partition(ResultScoreDoc[] array, int begin, int end) {
		int index = begin + random.nextInt(end - begin + 1);
		ResultScoreDoc pivot = array[index];
		swap(array, index, end);
		for (int i = index = begin; i < end; ++i)
			if (sorter.compare(array[i], pivot) <= 0)
				swap(array, index++, i);
		swap(array, index, end);
		return index;
	}

	private final void qsort(ResultScoreDoc[] array, int begin, int end) {
		if (end > begin) {
			int index = partition(array, begin, end);
			qsort(array, begin, index - 1);
			qsort(array, index + 1, end);
		}
	}

	public final void sort(ResultScoreDoc[] array) {
		qsort(array, 0, array.length - 1);
	}

}
