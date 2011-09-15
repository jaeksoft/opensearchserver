/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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

public class Expression implements Comparable<Expression> {

	private ExpressionToken[] tokens;

	public Expression(ExpressionToken[] tokens) {
		this.tokens = tokens;
	}

	@Override
	public int compareTo(Expression o) {
		return toString().compareTo(o.toString());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ExpressionToken token : tokens) {
			sb.append('_');
			sb.append(token.term);
		}
		sb.append('_');
		return sb.toString();
	}

	public int getSize() {
		return tokens.length;
	}

}
