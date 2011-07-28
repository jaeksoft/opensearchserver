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

package com.jaeksoft.searchlib.crawler.web.database;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.ExtensibleEnum;
import com.jaeksoft.searchlib.util.ExtensibleEnumItem;

public class UrlItemField extends ExtensibleEnumItem<UrlItemField> {

	public UrlItemField(ExtensibleEnum<UrlItemField> en, String name) {
		super(en, name);
	}

	void addFilterQuery(SearchRequest request, Object value)
			throws ParseException {
		StringBuffer sb = new StringBuffer();
		addQuery(sb, value);
		request.addFilter(sb.toString(), false);
	}

	void addFilterRange(SearchRequest request, Object from, Object to)
			throws ParseException {
		StringBuffer sb = new StringBuffer();
		addQueryRange(sb, from, to);
		request.addFilter(sb.toString(), false);
	}

	void addQuery(StringBuffer sb, Object value) {
		sb.append(" ");
		sb.append(name);
		sb.append(":");
		sb.append(value);
	}

	void addQueryRange(StringBuffer sb, Object from, Object to) {
		sb.append(" ");
		sb.append(name);
		sb.append(":[");
		sb.append(from);
		sb.append(" TO ");
		sb.append(to);
		sb.append("]");
	}

	@Override
	public String toString() {
		return name;
	}
}
