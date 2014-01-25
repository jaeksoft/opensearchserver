/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class ItemField extends ExtensibleEnumItem<ItemField> {

	public ItemField(ExtensibleEnum<ItemField> en, String name) {
		super(en, name);
	}

	public void addFilterQuery(AbstractSearchRequest request, Object value,
			boolean quote, boolean negative) throws ParseException {
		StringBuilder sb = new StringBuilder();
		addQuery(sb, value, quote);
		request.addFilter(sb.toString(), negative);
	}

	public void addFilterRange(AbstractSearchRequest request, Object from,
			Object to, boolean quote, boolean negative) throws ParseException {
		StringBuilder sb = new StringBuilder();
		addQueryRange(sb, from, to, quote);
		request.addFilter(sb.toString(), negative);
	}

	public void addSort(AbstractSearchRequest request, boolean desc) {
		request.addSort(0, name, desc);
	}

	public final static void addQuery(StringBuilder sb, String field,
			Object value, boolean quote) {
		sb.append(" ");
		sb.append(field);
		sb.append(":");
		if (quote)
			sb.append('"');
		sb.append(value);
		if (quote)
			sb.append('"');
	}

	public void addQuery(StringBuilder sb, Object value, boolean quote) {
		addQuery(sb, name, value, quote);
	}

	public void setQuery(AbstractSearchRequest request, Object value,
			boolean quote) {
		StringBuilder sb = new StringBuilder();
		addQuery(sb, value, quote);
		request.setQueryString(sb.toString());
	}

	public void addQueryRange(StringBuilder sb, Object from, Object to,
			boolean quote) {
		sb.append(" ");
		sb.append(name);
		sb.append(":[");
		if (quote)
			sb.append('"');
		sb.append(from);
		if (quote)
			sb.append('"');
		sb.append(" TO ");
		if (quote)
			sb.append('"');
		sb.append(to);
		if (quote)
			sb.append('"');
		sb.append("]");
	}

	@Override
	public String toString() {
		return name;
	}
}
