/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerFactory;
import com.jaeksoft.searchlib.index.FieldContent;

public class CompiledAnalyzer extends AbstractAnalyzer {

	private final TokenizerFactory tokenizer;
	private final FilterFactory[] filters;

	protected CompiledAnalyzer() {
		this.tokenizer = null;
		this.filters = null;
	}

	protected CompiledAnalyzer(TokenizerFactory sourceTokenizer,
			List<FilterFactory> sourceFilters, FilterScope scopeTarget)
			throws SearchLibException {
		sourceTokenizer.checkProperties();
		tokenizer = sourceTokenizer;
		List<FilterFactory> ff = new ArrayList<FilterFactory>();
		if (scopeTarget == FilterScope.INDEX)
			buildIndexList(sourceFilters, ff);
		else if (scopeTarget == FilterScope.QUERY)
			buildQueryList(sourceFilters, ff);
		filters = new FilterFactory[ff.size()];
		ff.toArray(filters);
	}

	private static void buildQueryList(List<FilterFactory> source,
			List<FilterFactory> target) throws SearchLibException {
		for (FilterFactory filter : source) {
			FilterScope scope = filter.getScope();
			if (scope == FilterScope.QUERY || scope == FilterScope.QUERY_INDEX) {
				filter.checkProperties();
				target.add(filter);
			}
		}
	}

	private static void buildIndexList(List<FilterFactory> source,
			List<FilterFactory> target) throws SearchLibException {
		for (FilterFactory filter : source) {
			FilterScope scope = filter.getScope();
			if (scope == FilterScope.INDEX || scope == FilterScope.QUERY_INDEX) {
				filter.checkProperties();
				target.add(filter);
			}
		}
	}

	public void justTokenize(String text, List<TokenTerm> tokenTerms)
			throws IOException {
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenizer.create(reader);
		ts = new TokenTermPopulateFilter(tokenTerms, ts);
		while (ts.incrementToken())
			;
		IOUtils.closeQuietly(ts);
	}

	@Override
	public TokenStream tokenStream(final String fieldname, final Reader reader) {
		try {
			TokenStream ts = tokenizer.create(reader);
			for (FilterFactory filter : filters)
				ts = filter.create(ts);
			return ts;
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isAnyToken(String fieldName, String value)
			throws IOException {
		if (tokenizer == null)
			return false;
		return tokenStream(fieldName, new StringReader(value)).incrementToken();
	}

	public List<DebugTokenFilter> test(String text) throws IOException,
			SearchLibException {
		List<DebugTokenFilter> list = new ArrayList<DebugTokenFilter>(0);
		if (text == null)
			return list;
		StringReader reader = new StringReader(text);
		DebugTokenFilter lastDebugTokenFilter = new DebugTokenFilter(tokenizer,
				tokenizer.create(reader));
		while (lastDebugTokenFilter.incrementToken())
			;
		list.add(lastDebugTokenFilter);
		for (FilterFactory filter : filters) {
			lastDebugTokenFilter.reset();
			DebugTokenFilter newDebugTokenFilter = new DebugTokenFilter(filter,
					filter.create(lastDebugTokenFilter));
			while (newDebugTokenFilter.incrementToken())
				;
			list.add(newDebugTokenFilter);
			lastDebugTokenFilter = newDebugTokenFilter;
		}
		return list;
	}

	public void extractTerms(String text, Set<String> termSet)
			throws IOException {
		if (text == null)
			return;
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(null, reader);
		ts = new TermSetTokenFilter(termSet, ts);
		while (ts.incrementToken())
			;
		IOUtils.closeQuietly(ts);
	}

	public void populate(String text, FieldContent fieldContent)
			throws IOException {
		if (text == null)
			return;
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(null, reader);
		ts = new FieldContentPopulateFilter(fieldContent, ts);
		while (ts.incrementToken())
			;
		IOUtils.closeQuietly(ts);
	}

	public void populate(String text, List<TokenTerm> tokenTerms)
			throws IOException {
		if (text == null)
			return;
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(null, reader);
		ts = new TokenTermPopulateFilter(tokenTerms, ts);
		while (ts.incrementToken())
			;
		IOUtils.closeQuietly(ts);
	}

	public int toBooleanQuery(String field, String text, BooleanQuery query,
			Occur occur) throws IOException {
		if (text == null)
			return 0;
		StringReader reader = new StringReader(text);
		TokenStream ts = tokenStream(null, reader);
		TokenQueryFilter ttqf = new TokenQueryFilter.BooleanQueryFilter(query,
				occur, field, 1.0F, ts);
		while (ttqf.incrementToken())
			;
		IOUtils.closeQuietly(ttqf);
		return ttqf.termCount;
	}

}
