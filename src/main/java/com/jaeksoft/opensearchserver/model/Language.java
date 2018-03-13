/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.jaeksoft.opensearchserver.model;

import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.utils.StringUtils;

import java.util.Collection;

public enum Language {

	en, fr, de, it;

	public final String title;
	public final String description;
	public final String content;
	public final String full;
	public final HighlighterDefinition.BreakIteratorDefinition breakIterator;
	public final HighlighterDefinition titleHighlight;
	public final HighlighterDefinition descriptionHighlight;
	public final HighlighterDefinition contentHighlight;

	Language() {

		title = "title" + StringUtils.capitalize(name());
		description = "description" + StringUtils.capitalize(name());
		content = "content" + StringUtils.capitalize(name());
		full = "full" + StringUtils.capitalize(name());

		breakIterator = new HighlighterDefinition.BreakIteratorDefinition(
				HighlighterDefinition.BreakIteratorDefinition.Type.sentence, name());

		titleHighlight = HighlighterDefinition.of(title)
				.setStoredField("title")
				.setMaxNoHighlightPassages(1)
				.setMaxPassages(1)
				.setBreak(breakIterator)
				.build();
		descriptionHighlight = HighlighterDefinition.of(description)
				.setStoredField("description")
				.setMaxNoHighlightPassages(5)
				.setMaxPassages(5)
				.setBreak(breakIterator)
				.build();
		contentHighlight = HighlighterDefinition.of(content)
				.setStoredField("content")
				.setMaxNoHighlightPassages(5)
				.setMaxPassages(5)
				.setBreak(breakIterator)
				.build();
	}

	public void highlights(final QueryBuilder builder) {
		builder.highlighter(title, titleHighlight);
		builder.highlighter(description, descriptionHighlight);
		builder.highlighter(content, contentHighlight);
	}

	public static Language findByName(final String name, final Language defaultValue) {
		try {
			return Language.valueOf(name);
		} catch (IllegalArgumentException | NullPointerException e) {
			return defaultValue;
		}
	}

	public static Language find(final Object value, final Language defaultValue) {
		if (value == null)
			return defaultValue;
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection) value;
			if (collection.isEmpty())
				return defaultValue;
			return find(collection.iterator().next(), defaultValue);
		}
		return findByName(value.toString(), defaultValue);
	}
}