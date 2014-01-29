/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.StringUtils;

public class ElisionFilter extends FilterFactory {

	private static final String DEFAULT_ARTICLE = "C c D d L l J j N n M m S s T t Qu qu QU";

	private final Set<String> articleSet = new TreeSet<String>();

	private static char[] apostrophes = { '\'', '\u2019' };

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.DEFAULT_ARTICLES, DEFAULT_ARTICLE, null,
				0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.DEFAULT_ARTICLES) {
			String[] articles = StringUtils.split(DEFAULT_ARTICLE);
			articleSet.clear();
			for (String article : articles)
				articleSet.add(article);
		}
	}

	@Override
	final public TokenStream create(final TokenStream tokenStream) {
		return new ElisionTokenFilter(tokenStream);
	}

	private class ElisionTokenFilter extends AbstractTermFilter {

		private ElisionTokenFilter(final TokenStream input) {
			super(input);
		}

		@Override
		public final boolean incrementToken() throws IOException {
			for (;;) {
				if (!input.incrementToken())
					return false;
				final String term = termAtt.toString();
				for (char apostrophe : apostrophes) {
					final int i = term.indexOf(apostrophe);
					if (i != -1) {
						final String check = term.substring(0, i);
						if (articleSet.contains(check))
							return createToken(term.substring(i + 1));
					}
				}
				return true;
			}
		}
	}
}
