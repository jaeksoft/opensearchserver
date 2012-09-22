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

package com.jaeksoft.searchlib.util.map;

import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class SourceField implements Comparable<SourceField> {

	final private String[] names;

	final private char separator;

	public SourceField(String name) {
		names = new String[1];
		names[0] = name;
		separator = '|';
	}

	public SourceField(String name, char separator) {
		names = StringUtils.split(name, separator);
		this.separator = separator;
	}

	@Override
	final public boolean equals(Object o) {
		if (!(o instanceof SourceField))
			return false;
		SourceField s2 = (SourceField) o;
		if (s2.names.length != names.length)
			return false;
		for (int i = 0; i < names.length; i++)
			if (!names[i].equals(s2.names[i]))
				return false;
		return true;
	}

	@Override
	final public int compareTo(SourceField o) {
		int l = names.length;
		if (o.names.length < l)
			l = o.names.length;
		for (int i = 0; i < l; i++) {
			int c = names[i].compareTo(o.names[i]);
			if (c != 0)
				return c;
		}
		return o.names.length - names.length;
	}

	final public String toXmlAttribute() {
		if (names.length == 1)
			return names[0];
		StringBuffer sb = new StringBuffer(names[0]);
		for (int i = 1; i < names.length; i++) {
			sb.append(separator);
			sb.append(names[i]);
		}
		return sb.toString();
	}

	final public boolean isUnique() {
		return names.length == 1;
	}

	final public String getUniqueName() {
		return names[0];
	}

	final public FieldContent getUniqueString(IndexDocument source) {
		return source.getField(names[0]);
	}

	final public FieldValueItem[] getUniqueString(ResultDocument source) {
		return source.getValueArray(names[0]);
	}

	final public String getConcatString(IndexDocument source,
			IndexDocument target) {
		StringBuffer sb = new StringBuffer();
		for (String name : names) {
			FieldValueItem fvi = source.getFieldValue(name, 0);
			if (fvi == null)
				fvi = target.getFieldValue(name, 0);
			if (fvi != null) {
				String value = fvi.getValue();
				if (value != null) {
					if (sb.length() != 0)
						sb.append(separator);
					sb.append(value);
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}

	public String getConcatString(ResultDocument source, IndexDocument target) {
		StringBuffer sb = new StringBuffer();
		for (String name : names) {
			String value = source.getValueContent(name, 0);
			if (value == null) {
				FieldValueItem fvi = target.getFieldValue(name, 0);
				if (fvi != null)
					value = fvi.getValue();
			}
			if (value != null) {
				if (sb.length() != 0)
					sb.append(separator);
				sb.append(value);
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}

	public void addReturnField(SearchRequest searchRequest)
			throws SearchLibException {
		for (String name : names)
			searchRequest.addReturnField(name);
	}

}
