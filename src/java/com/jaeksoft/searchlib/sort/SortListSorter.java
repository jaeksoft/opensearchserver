/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.schema.FieldList;

public class SortListSorter extends SorterAbstract {

	private SorterAbstract[] sorterList;

	protected SortListSorter(FieldList<SortField> sortFieldList) {
		sorterList = new SorterAbstract[sortFieldList.size()];
		int i = 0;
		for (SortField sortField : sortFieldList)
			sorterList[i++] = sortField.getSorter();
	}

	@Override
	protected int compare(ResultScoreDoc doc1, Object value1,
			ResultScoreDoc doc2, Object value2) {
		Object[] values1 = doc1.getSortValues();
		Object[] values2 = doc2.getSortValues();
		int i = 0;
		for (SorterAbstract sorter : sorterList) {
			int c = sorter.compare(doc1, values1[i], doc2, values2[i]);
			if (c != 0)
				return c;
		}
		return 0;
	}

}
