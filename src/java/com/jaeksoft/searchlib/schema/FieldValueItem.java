/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Fieldable;

public class FieldValueItem {

	final private String value;

	final private Float boost;

	final private FieldValueOriginEnum origin;

	final public static FieldValueItem[] emptyArray = new FieldValueItem[0];

	public FieldValueItem(FieldValueOriginEnum origin, String value) {
		this.value = value;
		this.boost = null;
		this.origin = origin;
	}

	public FieldValueItem(FieldValueOriginEnum origin, String value, Float boost) {
		this.value = value;
		if (boost != null && boost == 1.0f)
			boost = null;
		this.boost = boost;
		this.origin = origin;
	}

	private FieldValueItem(Fieldable fieldable) {
		this.value = fieldable.stringValue();
		float b = fieldable.getBoost();
		this.boost = b == 1.0f ? null : b;
		this.origin = FieldValueOriginEnum.STORAGE;
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

	/**
	 * @return the origin
	 */
	final public FieldValueOriginEnum getOrigin() {
		return origin;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
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

	final public static FieldValueItem[] buildArray(
			FieldValueOriginEnum origin, String[] values) {
		FieldValueItem[] array = new FieldValueItem[values.length];
		int i = 0;
		for (String value : values)
			array[i++] = new FieldValueItem(origin, value);
		return array;
	}

	final public static FieldValueItem[] buildArray(
			FieldValueOriginEnum origin, String value) {
		FieldValueItem[] array = new FieldValueItem[1];
		array[0] = new FieldValueItem(origin, value);
		return array;
	}

	final public static List<String> buildArrayList(
			FieldValueItem[] fieldValueItemArray) {
		if (fieldValueItemArray == null)
			return null;
		List<String> list = new ArrayList<String>(fieldValueItemArray.length);
		for (FieldValueItem item : fieldValueItemArray)
			list.add(item.value);
		return list;
	}

}
