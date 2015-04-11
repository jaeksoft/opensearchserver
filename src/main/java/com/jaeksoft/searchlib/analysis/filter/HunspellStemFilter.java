/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.HunspellUtils;

public class HunspellStemFilter extends FilterFactory {

	private String dict_path = null;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.HUNSPELL_DICT_PATH, "", null, 30, 1);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.HUNSPELL_DICT_PATH)
			dict_path = value;
	}

	protected TokenStream newTokenFilter(TokenStream input,
			HunspellUtils.Api hunspell) {
		return new HunspellStemTokenFilter(input, hunspell);
	}

	@Override
	final public TokenStream create(TokenStream input)
			throws SearchLibException {
		HunspellUtils.Api hunspell = HunspellUtils.getBridj(dict_path);
		return newTokenFilter(input, hunspell);
	}

	public static class HunspellStemTokenFilter extends AbstractTermFilter {

		protected final HunspellUtils.Api hunspell;

		private List<String> wordQueue = null;

		private String currentTerm = null;

		private int currentPos = 0;

		public HunspellStemTokenFilter(TokenStream input,
				HunspellUtils.Api hunspell) {
			super(input);
			this.hunspell = hunspell;
		}

		private final boolean popToken() {
			if (currentTerm != null) {
				createToken(currentTerm);
				currentTerm = null;
				return true;
			}
			if (wordQueue == null)
				return false;
			if (currentPos == wordQueue.size())
				return false;
			createToken(wordQueue.get(currentPos++));
			return true;
		}

		protected List<String> getWords(String currentTerm) {
			synchronized (hunspell) {
				return hunspell.stem(currentTerm);
			}
		}

		private final void createTokens() {
			currentTerm = termAtt.toString();
			wordQueue = getWords(currentTerm);
			if (wordQueue != null && wordQueue.size() > 0)
				currentTerm = null;
			currentPos = 0;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			for (;;) {
				if (popToken())
					return true;
				if (!input.incrementToken())
					return false;
				createTokens();
			}
		}
	}

}
