/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.snippet;

import org.w3c.dom.NamedNodeMap;

public class SentenceFragmenter extends FragmenterAbstract {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5789364707381845312L;

	@Override
	public void setAttributes(NamedNodeMap attr) {
	}

	@Override
	public void check(String originalText) {
		int pos = 0;
		char[] chars = originalText.toCharArray();
		boolean nextSpaceIsSplit = false;
		for (char ch : chars) {
			if (nextSpaceIsSplit)
				if (Character.isWhitespace(ch))
					addSplit(pos + 1);
			switch (ch) {
			case '.':
			case '?':
			case '!':
				nextSpaceIsSplit = true;
				break;
			default:
				nextSpaceIsSplit = false;
				break;
			}
			pos++;
		}
	}

	@Override
	protected FragmenterAbstract newInstance() {
		return new SentenceFragmenter();
	}
}
