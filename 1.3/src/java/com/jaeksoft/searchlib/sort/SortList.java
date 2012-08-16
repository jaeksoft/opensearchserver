/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.FieldList;

public class SortList implements CacheKeyInterface<SortList> {

	private FieldList<SortField> sortFieldList;

	public SortList() {
		sortFieldList = new FieldList<SortField>();
	}

	public SortList(SortList sortList) {
		sortFieldList = new FieldList<SortField>(sortList.sortFieldList);
	}

	public void add(SortField sortField) {
		sortFieldList.add(sortField);
	}

	public void add(String sortString) {
		sortFieldList.add(SortField.newSortField(sortString));
	}

	public void add(String fieldName, boolean desc) {
		sortFieldList.add(new SortField(fieldName, desc));
	}

	public FieldList<SortField> getFieldList() {
		return sortFieldList;
	}

	public boolean isScore() {
		if (sortFieldList.size() == 0)
			return true;
		for (SortField field : sortFieldList)
			if (field.isScore())
				return true;
		return false;
	}

	public SorterAbstract getSorter(DocIdInterface collector, ReaderLocal reader)
			throws IOException {
		if (sortFieldList.size() == 0)
			return new DescScoreSorter(collector);
		return new SortListSorter(sortFieldList, collector, reader);
	}

	@Override
	public int compareTo(SortList o) {
		return sortFieldList.compareTo(o.sortFieldList);
	}

	public void remove(SortField sortField) {
		sortFieldList.remove(sortField);
	}

}
