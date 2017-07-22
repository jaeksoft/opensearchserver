/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
 **/

package com.jaeksoft.searchlib.webservice.query.spellcheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckItem;
import com.jaeksoft.searchlib.spellcheck.SuggestionItem;
import com.jaeksoft.searchlib.webservice.CommonResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SpellcheckResult extends CommonResult {

	@XmlElement(name = "field")
	@JsonProperty("fields")
	public List<Field> fields;

	public SpellcheckResult() {
		fields = null;
	}

	public SpellcheckResult(ResultSpellCheck result, String query) {
		List<SpellCheck> spellChecks = result.getSpellCheckList();
		if (spellChecks == null)
			return;
		fields = new ArrayList<Field>(spellChecks.size());
		for (SpellCheck spellCheck : spellChecks)
			fields.add(new Field(spellCheck));
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@XmlType(name = "SpellcheckField")
	public static class Field {

		@XmlElement(name = "fieldName")
		@JsonProperty("fieldName")
		public String fieldName;

		@XmlElement(name = "word")
		@JsonProperty("words")
		public List<Word> words;

		public String suggestion;

		public Field() {
			words = null;
			fieldName = null;
		}

		public Field(SpellCheck spellCheck) {
			fieldName = spellCheck.getFieldName();
			List<SpellCheckItem> spellCheckItems = spellCheck.getList();
			if (spellCheckItems == null)
				return;
			words = new ArrayList<Word>(spellCheckItems.size());
			for (SpellCheckItem spellCheckItem : spellCheckItems)
				words.add(new Word(spellCheckItem));
			suggestion = spellCheck.getSuggestion();
		}
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Word {

		@XmlElement(name = "word")
		@JsonProperty("word")
		public String word;

		@XmlElement(name = "suggest")
		@JsonProperty("suggest")
		public List<Suggestion> suggest;

		public Word() {
			word = null;
			suggest = null;
		}

		public Word(SpellCheckItem spellItem) {
			word = spellItem.getWord();
			SuggestionItem[] suggestions = spellItem.getSuggestions();
			if (suggestions == null)
				return;
			suggest = new ArrayList<Suggestion>(suggestions.length);
			for (SuggestionItem suggestion : suggestions)
				suggest.add(new Suggestion(suggestion));

		}
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Suggestion {

		@XmlElement(name = "term")
		public String term;

		@XmlElement(name = "freq")
		public int freq;

		public Suggestion() {
			term = null;
			freq = 0;
		}

		public Suggestion(SuggestionItem suggestionItem) {
			term = suggestionItem.getTerm();
			freq = suggestionItem.getFreq();

		}
	}
}
