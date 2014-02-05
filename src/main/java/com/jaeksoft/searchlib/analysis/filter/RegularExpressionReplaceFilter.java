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
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class RegularExpressionReplaceFilter extends FilterFactory {

	private Pattern pattern = null;
	private String replace = null;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.REGULAR_EXPRESSION, "", null, 30, 1);
		addProperty(ClassPropertyEnum.REGULAR_EXPRESSION_REPLACEMENT, "", null,
				30, 1);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.REGULAR_EXPRESSION)
			pattern = Pattern.compile(value);
		else if (prop == ClassPropertyEnum.REGULAR_EXPRESSION_REPLACEMENT)
			replace = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new RegularExpressionReplaceTokenFilter(pattern, replace,
				tokenStream);
	}

	public static class RegularExpressionReplaceTokenFilter extends
			AbstractTermFilter {

		private final Pattern pattern;
		private final String replace;

		protected RegularExpressionReplaceTokenFilter(Pattern pattern,
				String replace, TokenStream input) {
			super(input);
			this.pattern = pattern;
			this.replace = replace;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			for (;;) {
				if (!input.incrementToken())
					return false;
				synchronized (pattern) {
					if (createToken(pattern.matcher(termAtt.toString())
							.replaceAll(replace)))
						return true;
				}
			}
		}
	}

	public final static void main(String[] args) {
		System.out.println("january, 1999".replaceAll("^.* ([0-9]*)$", "$1"));
		System.out.println(Pattern.compile("^.* ([0-9]*)$")
				.matcher("january, 1999").replaceAll("$1"));
	}
}
