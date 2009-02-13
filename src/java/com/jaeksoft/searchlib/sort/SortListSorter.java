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

public class SortListSorter implements SorterInterface {

	private boolean[] descendant;

	protected SortListSorter(SortList sortList) {
		descendant = sortList.newDescArray();
	}

	public boolean isBefore(ResultScoreDoc doc1, ResultScoreDoc doc2) {
		String[] values1 = doc1.getSortValues();
		String[] values2 = doc1.getSortValues();
		for (int i = 0; i < values1.length; i++) {
			String v1 = values1[i];
			String v2 = values2[i];
			boolean desc = descendant[i];
			int c = 0;
			// Take care of null values
			if (v1 == null) {
				if (v2 != null)
					c = -1;
			} else if (v2 == null) {
				c = 1;
			} else
				c = values1[i].compareTo(values2[i]);
			if (desc) {
				if (c > 0)
					return true;
				if (c < 0)
					return false;
			} else {
				if (c > 0)
					return false;
				if (c < 0)
					return true;
			}
		}
		return false;
	}
}
