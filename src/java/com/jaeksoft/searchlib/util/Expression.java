/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

public class Expression implements Comparable<Expression> {

	private String[] words;
	private int offset;

	public Expression(String w) {
		this.words = w.split("\\p{Space}+");
		this.offset = 0;
	}

	public Expression(String[] w, int offset) {
		this.words = w;
		this.offset = offset;
	}

	public int getSize() {
		return words.length - offset;
	}

	public String get(int n) {
		if (n >= getSize())
			return null;
		return words[n + offset];
	}

	@Override
	public int compareTo(Expression o) {
		return toString().compareTo(o.toString());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = offset; i < words.length; i++) {
			sb.append('_');
			sb.append(words[i]);
		}
		sb.append('_');
		sb.append(getSize());
		sb.append('_');
		return sb.toString();
	}

}
