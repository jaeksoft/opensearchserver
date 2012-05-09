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

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.result.ResultScoreDoc;

public class AscStringIndexSorter extends SorterAbstract {

	private StringIndex stringIndex;

	public AscStringIndexSorter(StringIndex stringIndex) {
		this.stringIndex = stringIndex;
	}

	@Override
	protected int compare(ResultScoreDoc doc1, ResultScoreDoc doc2) {
		String value1 = stringIndex.lookup[stringIndex.order[doc1.doc]];
		String value2 = stringIndex.lookup[stringIndex.order[doc2.doc]];
		if (value1 == null) {
			if (value2 == null)
				return 0;
			else
				return -1;
		} else if (value2 == null)
			return 1;
		return value1.compareTo(value2);
	}

}
