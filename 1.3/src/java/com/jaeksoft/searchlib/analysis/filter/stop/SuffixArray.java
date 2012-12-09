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

package com.jaeksoft.searchlib.analysis.filter.stop;

import java.io.IOException;

public class SuffixArray extends PrefixArray {

	public SuffixArray(WordArray wordArray, boolean ignoreCase,
			String tokenSeparator) throws IOException {
		super(wordArray, ignoreCase, tokenSeparator);
	}

	@Override
	protected String putWord(String word) {
		StringBuffer sb = new StringBuffer();
		if (tokenSeparator != null && tokenSeparator.length() > 0)
			sb.append(tokenSeparator);
		sb.append(word);
		return sb.toString();
	}

	@Override
	public boolean match(String term) {
		for (String fix : fixArray)
			if (term.endsWith(fix))
				return true;
		return wordSet.contains(term);
	}
}
