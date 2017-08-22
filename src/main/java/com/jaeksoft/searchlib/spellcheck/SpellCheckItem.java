/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.spellcheck;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.ReaderLocal;

import java.io.IOException;

public class SpellCheckItem {

	private String word;
	private SuggestionItem[] suggestions;
	private SuggestionItem higher;

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

	public String getHigher() {
		if (higher == null)
			return null;
		return higher.getTerm();
	}

	public void computeFrequency(ReaderLocal reader, String fieldName) throws IOException, SearchLibException {
		if (suggestions == null)
			return;
		for (SuggestionItem suggestionItem : suggestions) {
			suggestionItem.computeFrequency(reader, fieldName);
			if (higher == null)
				higher = suggestionItem;
			else if (suggestionItem.getFreq() > higher.getFreq())
				higher = suggestionItem;
		}

	}
}
