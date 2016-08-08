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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.SpellChecker;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SpellCheckRequest;

public class SpellCheck implements Iterable<SpellCheckItem> {

	private List<SpellCheckItem> spellCheckItems;

	private String fieldName;

	private String suggestion;

	public SpellCheck(ReaderLocal reader, SpellCheckRequest request,
			SpellCheckField spellCheckField) throws ParseException,
			SyntaxError, IOException, SearchLibException {
		fieldName = spellCheckField.getName();
		SpellChecker spellchecker = reader.getSpellChecker(fieldName);
		Set<String> wordSet = new LinkedHashSet<String>();

		Set<Term> set = request.getTermSet(spellCheckField.getName());
		for (Term term : set)
			if (term.field().equals(fieldName))
				wordSet.add(term.text());
		int suggestionNumber = spellCheckField.getSuggestionNumber();
		float minScore = spellCheckField.getMinScore();
		synchronized (spellchecker) {
			spellchecker.setAccuracy(minScore);
			spellchecker.setStringDistance(spellCheckField.getStringDistance()
					.getNewInstance());
			spellCheckItems = new ArrayList<SpellCheckItem>();
			for (String word : wordSet) {
				String[] suggestions = spellchecker.suggestSimilar(word,
						suggestionNumber);
				int s = 1;
				if (suggestions != null)
					s += suggestions.length;
				SuggestionItem[] suggestionItems = new SuggestionItem[s];
				int i = 0;
				suggestionItems[i++] = new SuggestionItem(word);
				if (suggestions != null) {
					for (String suggestion : suggestions)
						suggestionItems[i++] = new SuggestionItem(suggestion);
					spellCheckItems.add(new SpellCheckItem(word,
							suggestionItems));
				}
			}
		}
		List<String> highers = new ArrayList<String>(spellCheckItems.size());
		for (SpellCheckItem spellcheckItem : spellCheckItems) {
			spellcheckItem.computeFrequency(reader, fieldName);
			String higher = spellcheckItem.getHigher();
			if (higher != null)
				highers.add(higher);
		}
		suggestion = StringUtils.join(highers, ' ');
	}

	public String getFieldName() {
		return fieldName;
	}

	public List<SpellCheckItem> getList() {
		return spellCheckItems;
	}

	public String getSuggestion() {
		return suggestion;
	}

	@Override
	public Iterator<SpellCheckItem> iterator() {
		return spellCheckItems.iterator();
	}

}
