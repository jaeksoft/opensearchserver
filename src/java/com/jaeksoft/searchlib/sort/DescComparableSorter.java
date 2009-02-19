/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.ResultScoreDoc;

public class DescComparableSorter<T extends Comparable<T>> extends
		SorterAbstract {

	@SuppressWarnings("unchecked")
	protected int compare(ResultScoreDoc doc1, Object value1,
			ResultScoreDoc doc2, Object value2) {
		if (value1 == null) {
			if (value2 == null)
				return 0;
			else
				return 1;
		} else if (value2 == null)
			return -1;
		T v1 = (T) value1;
		T v2 = (T) value2;
		return v2.compareTo(v1);
	}
}
