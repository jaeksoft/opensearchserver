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

import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchResults {

	private final List<SearchResult> searchResults;
	private final Double totalTime;
	private final Long numDocs;

	public SearchResults(final ResultDefinition.WithObject<UrlRecord> results, final Language language) {
		searchResults = from(results.documents, language);
		totalTime = (double) results.getTimer().totalTime / 1000;
		numDocs = results.getTotalHits();
	}

	private static List<SearchResult> from(final List<ResultDocumentObject<UrlRecord>> documentResults,
			final Language language) {
		if (documentResults == null)
			return null;
		if (documentResults.isEmpty())
			return Collections.emptyList();
		final List<SearchResult> results = new ArrayList<>(documentResults.size());
		documentResults.forEach(doc -> results.add(new SearchResult(doc, language)));
		return results;
	}

	public List<SearchResult> getResults() {
		return searchResults;
	}

	public Double getTotalTime() {
		return totalTime;
	}

	public Long getNumDocs() {
		return numDocs;
	}

	public static class SearchResult {

		private final String url;
		private final String urlDisplay;
		private final String title;
		private final String description;
		private final String content;

		public SearchResult(final ResultDocumentObject<UrlRecord> document, final Language language) {
			url = document.record.urlStore;
			urlDisplay = url == null ? null : LinkUtils.urlHostPathWrapReduce(url, 70);
			title = truncate(getBestHighlight(document, language.title, "title"), 100);
			description = truncate(getBestHighlight(document, language.description, "description"), 250);
			content = truncate(getBestHighlight(document, language.content, "content"), 250);
		}

		private static String getBestHighlight(final ResultDocumentObject<UrlRecord> document, final String... fields) {
			if (document.highlights == null)
				return null;
			String firstNonNull = null;
			for (String field : fields) {
				final String value = document.highlights.get(field);
				if (StringUtils.isBlank(value))
					continue;
				if (firstNonNull == null)
					firstNonNull = value;
				if (value.contains("<b>"))
					return value;
			}
			return firstNonNull;
		}

		private static String truncate(final String text, final int maxSize) {
			if (text == null || text.length() <= maxSize)
				return text;
			final String[] parts = StringUtils.split(text);
			final StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String part : parts) {
				if (sb.length() + part.length() >= maxSize)
					break;
				if (first)
					first = false;
				else
					sb.append(' ');
				sb.append(part);
			}
			if (first)
				sb.append(text.substring(0, maxSize - 1));
			sb.append('…');
			return sb.toString();
		}

		public String getUrl() {
			return url;
		}

		public String getUrlDisplay() {
			return urlDisplay;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getContent() {
			return content;
		}

	}
}
