/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

public class MimeUtils {

	public static String extractContentTypeCharset(String contentType) {
		if (contentType == null)
			return null;
		int i = contentType.indexOf("charset=");
		if (i == -1)
			return null;
		contentType = contentType.substring(i + 8);
		i = contentType.indexOf(';');
		if (i == -1)
			return contentType;
		return contentType.substring(0, i);
	}

	public static String extractContentBaseType(String contentType) {
		if (contentType == null)
			return null;
		int i = contentType.indexOf(';');
		if (i == -1)
			return contentType;
		return contentType.substring(0, i);
	}

	public static void main(String[] args) {
		String[] tests = { "text/plain", "text/html; charset=utf-8" };
		for (String test : tests)
			System.out.println(test + " |" + extractContentBaseType(test)
					+ "| => |" + extractContentTypeCharset(test) + "|");
	}
}
