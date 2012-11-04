/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class GroupAllTokensFilter extends FilterFactory {

	public class GroupAllTokenFilter extends AbstractTermFilter {

		private StringBuffer buffer;

		private Integer startOffset = null;

		private Integer endOffset = null;

		protected GroupAllTokenFilter(TokenStream input) {
			super(input);
			buffer = new StringBuffer();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (buffer == null)
				return false;
			current = captureState();
			while (input.incrementToken()) {
				if (startOffset == null)
					startOffset = offsetAtt.startOffset();
				String t = termAtt.term();
				buffer.append(t);
				if (tokenSeparator != null)
					buffer.append(tokenSeparator);
				endOffset = offsetAtt.endOffset();
			}
			String term = buffer.toString().trim();
			if (term.length() == 0)
				return false;
			createToken(term, 1, startOffset, endOffset);
			buffer = null;
			return true;
		}
	}

	private String tokenSeparator;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.TOKEN_SEPARATOR, " ", null);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.TOKEN_SEPARATOR)
			tokenSeparator = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new GroupAllTokenFilter(tokenStream);
	}
}
