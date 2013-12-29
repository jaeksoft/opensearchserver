/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.query;

import com.jaeksoft.searchlib.util.StringUtils;

public class QueryUtils {

	public final static String[] CONTROL_CHARS = { "\\", "^", "\"", "~", ":" };

	public final static String[] RANGE_CHARS = { "(", ")", "{", "}", "[", "]" };

	public final static String[] AND_OR_NOT_CHARS = { "+", "-", "&&", "||", "!" };

	public final static String[] WILDCARD_CHARS = { "*", "?" };

	final public static String escapeQuery(String query, String[] escapeChars) {
		for (String s : escapeChars) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				sb.append('\\');
				sb.append(s.charAt(i));
			}
			query = query.replace(s, sb.toString());
		}
		return query;
	}

	final public static String escapeQuery(String query) {
		query = escapeQuery(query, CONTROL_CHARS);
		query = escapeQuery(query, RANGE_CHARS);
		query = escapeQuery(query, AND_OR_NOT_CHARS);
		query = escapeQuery(query, WILDCARD_CHARS);
		return query;
	}

	final public static String replaceControlChars(String query,
			String[] controlChars, String replaceChars) {
		for (String s : controlChars)
			query = query.replace(s, replaceChars);
		return query;
	}

	final public static String replaceControlChars(String query) {
		query = replaceControlChars(query, CONTROL_CHARS, " ");
		query = replaceControlChars(query, RANGE_CHARS, " ");
		query = replaceControlChars(query, AND_OR_NOT_CHARS, " ");
		query = replaceControlChars(query, WILDCARD_CHARS, " ");
		return StringUtils.replaceConsecutiveSpaces(query, " ");
	}

}
