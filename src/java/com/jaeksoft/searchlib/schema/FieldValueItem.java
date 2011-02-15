/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Fieldable;

public class FieldValueItem {

	final private String value;

	final private Float boost;

	public FieldValueItem(String value) {
		this.value = value;
		this.boost = null;
	}

	public FieldValueItem(String value, Float boost) {
		this.value = value;
		if (boost != null && boost == 1.0f)
			boost = null;
		this.boost = boost;
	}

	private FieldValueItem(Fieldable fieldable) {
		this.value = fieldable.stringValue();
		float b = fieldable.getBoost();
		this.boost = b == 1.0f ? null : b;
	}

	/**
	 * @return the value
	 */
	final public String getValue() {
		return value;
	}

	/**
	 * @return the boost
	 */
	final public Float getBoost() {
		return boost;
	}

	@Override
	public boolean equals(Object obj) {
		FieldValueItem item = (FieldValueItem) obj;
		if (value == null)
			return (item.value == null);
		return value.equals(item.value);
	}

	final public static FieldValueItem[] buildArray(Fieldable[] fieldables) {
		FieldValueItem[] array = new FieldValueItem[fieldables.length];
		int i = 0;
		for (Fieldable fieldable : fieldables)
			array[i++] = new FieldValueItem(fieldable);
		return array;
	}

	final public static List<String> buildArrayList(
			List<FieldValueItem> fieldValueItemList) {
		if (fieldValueItemList == null)
			return null;
		List<String> list = new ArrayList<String>(fieldValueItemList.size());
		for (FieldValueItem item : fieldValueItemList)
			list.add(item.value);
		return list;
	}

}
