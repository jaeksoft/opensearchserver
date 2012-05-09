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

import org.apache.lucene.search.Sort;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
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

	public Sort getLuceneSort() {
		if (sortFieldList.size() == 0)
			return null;
		org.apache.lucene.search.SortField[] sortFields = new org.apache.lucene.search.SortField[sortFieldList
				.size()];
		int i = 0;
		for (SortField field : sortFieldList)
			sortFields[i++] = field.getLuceneSortField();
		return new Sort(sortFields);
	}

	public SorterAbstract getSorter(ReaderLocal reader) throws IOException {
		if (sortFieldList.size() == 0)
			return new DescScoreSorter();
		return new SortListSorter(sortFieldList, reader);
	}

	@Override
	public int compareTo(SortList o) {
		return sortFieldList.compareTo(o.sortFieldList);
	}

	public void remove(SortField sortField) {
		sortFieldList.remove(sortField);
	}

}
