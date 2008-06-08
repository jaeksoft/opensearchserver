/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import org.apache.lucene.search.Sort;

public class SortField extends Field {

	private static final long serialVersionUID = -476489382677039069L;

	private boolean desc;

	public SortField(String name, boolean desc) {
		super(name);
		this.desc = desc;
	}

	private SortField(SortField sortField) {
		super(sortField.name);
		desc = sortField.desc;
	}

	public boolean isDesc() {
		return desc;
	}

	@Override
	public Object clone() {
		return new SortField(this);
	}

	public void toString(StringBuffer sb) {
		if (desc)
			sb.append('-');
		else
			sb.append('+');
		sb.append(name);
	}

	public static SortField newSortField(String requestSort) {
		int c = requestSort.charAt(0);
		boolean desc = (c == '-');
		String name = (c == '+' || c == '-') ? requestSort.substring(1)
				: requestSort;
		return new SortField(name, desc);
	}

	public static Sort getLuceneSort(FieldList<SortField> sortFieldList) {
		org.apache.lucene.search.SortField[] sortFields = new org.apache.lucene.search.SortField[sortFieldList
				.size()];
		int i = 0;
		for (SortField field : sortFieldList)
			sortFields[i++] = new org.apache.lucene.search.SortField(
					field.name, field.desc);
		return new Sort(sortFields);
	}

	public static String getSortKey(FieldList<SortField> sortFieldList) {
		StringBuffer sb = new StringBuffer();
		for (SortField field : sortFieldList)
			field.toString(sb);
		return sb.toString();
	}
}
