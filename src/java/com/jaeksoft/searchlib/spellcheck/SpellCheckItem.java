/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.spellcheck;

import java.io.IOException;

import com.jaeksoft.searchlib.index.ReaderLocal;

public class SpellCheckItem {

	private String word;
	private SuggestionItem[] suggestions;

	public SpellCheckItem(String word, SuggestionItem[] suggestions) {
		this.word = word;
		this.suggestions = suggestions;
	}

	public String getWord() {
		return word;
	}

	public SuggestionItem[] getSuggestions() {
		return suggestions;
	}

	public void computeFrequency(ReaderLocal reader, String fieldName)
			throws IOException {
		if (suggestions == null)
			return;
		for (SuggestionItem suggestionItem : suggestions)
			suggestionItem.computeFrequency(reader, fieldName);

	}
}
