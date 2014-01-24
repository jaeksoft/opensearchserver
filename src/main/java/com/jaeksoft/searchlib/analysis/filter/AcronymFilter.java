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
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.StringUtils;

public class AcronymFilter extends FilterFactory {

	private boolean expand = false;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.EXPAND, Boolean.FALSE.toString(),
				ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.EXPAND)
			expand = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new AcronymTokenFilter(tokenStream);
	}

	public class AcronymTokenFilter extends AbstractTermFilter {

		private final Deque<String> termsQueue;
		private int increment;

		public AcronymTokenFilter(final TokenStream input) {
			super(input);
			termsQueue = new ArrayDeque<String>(0);
		}

		private final boolean popToken() {
			if (termsQueue.isEmpty())
				return false;
			final String term = termsQueue.poll();
			createToken(term, termsQueue.isEmpty() ? increment : 0,
					offsetAtt.startOffset(), offsetAtt.endOffset(),
					typeAtt.type(), flagsAtt.getFlags());
			return true;
		}

		final private String[] checkAcronyms(final String currentTerm) {
			String[] letters = StringUtils.split(currentTerm, '.');
			for (String letter : letters)
				if (letter.length() != 1)
					return null;
			return letters;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			for (;;) {
				if (popToken())
					return true;
				if (!input.incrementToken())
					return false;
				String term = termAtt.toString();
				String[] letters = checkAcronyms(term);
				if (letters == null)
					return true;
				increment = posIncrAtt.getPositionIncrement();
				if (expand)
					termsQueue.offer(term);
				termsQueue.offer(StringUtils.join(letters));
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Create token: " + this);
			return sb.toString();
		}
	}

}
