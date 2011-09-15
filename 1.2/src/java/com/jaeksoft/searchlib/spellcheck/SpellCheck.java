/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.spell.SpellChecker;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.util.External;

public class SpellCheck implements Externalizable, Iterable<SpellCheckItem>,
		External.Collecter<SpellCheckItem> {

	private List<SpellCheckItem> spellCheckItems;

	private String fieldName;

	public SpellCheck(ResultSingle result, SpellCheckField spellCheckField)
			throws ParseException, SyntaxError, IOException, SearchLibException {
		SearchRequest searchRequest = result.getSearchRequest();
		ReaderLocal reader = result.getReader();
		fieldName = spellCheckField.getName();
		SpellChecker spellchecker = reader.getSpellChecker(fieldName);
		Set<Term> set = new LinkedHashSet<Term>();
		Set<String> wordSet = new LinkedHashSet<String>();
		searchRequest.getQuery().extractTerms(set);
		for (Term term : set)
			if (term.field().equals(fieldName))
				wordSet.add(term.text());
		int suggestionNumber = spellCheckField.getSuggestionNumber();
		float minScore = spellCheckField.getMinScore();
		spellchecker.setAccuracy(minScore);
		spellCheckItems = new ArrayList<SpellCheckItem>();
		for (String word : wordSet) {
			String[] suggestions = spellchecker.suggestSimilar(word,
					suggestionNumber);
			if (suggestions != null)
				if (suggestions.length > 0)
					spellCheckItems.add(new SpellCheckItem(word, suggestions));
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public List<SpellCheckItem> getList() {
		return spellCheckItems;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<SpellCheckItem> iterator() {
		return spellCheckItems.iterator();
	}

	@Override
	public void addObject(SpellCheckItem object) {
		// TODO Auto-generated method stub

	}

}
