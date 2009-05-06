/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.function.token;

public abstract class Token {

	public int size;

	private char[] additionalChars;

	protected Token(char[] chars, int pos, char[] additionalChars) {
		this.additionalChars = additionalChars;
		StringBuffer token = new StringBuffer();
		size = 0;
		while (pos < chars.length) {
			char ch = chars[pos++];
			if (!charIsValid(ch))
				break;
			token.append(ch);
			size++;
		}
		set(token);
	}

	protected boolean charIsValid(char ch) {
		if (additionalChars == null)
			return false;
		for (char c : additionalChars)
			if (c == ch)
				return true;
		return false;
	}

	protected abstract void set(StringBuffer token);

}
