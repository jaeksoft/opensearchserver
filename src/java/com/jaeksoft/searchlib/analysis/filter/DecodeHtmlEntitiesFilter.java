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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.FilterFactory;

public class DecodeHtmlEntitiesFilter extends FilterFactory {

	public class DecodeHtmlEntitiesTokenFilter extends AbstractTermFilter {

		protected DecodeHtmlEntitiesTokenFilter(TokenStream input) {
			super(input);

		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				createToken(StringEscapeUtils.unescapeHtml(termAtt.term()));
				return true;
			}
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new DecodeHtmlEntitiesTokenFilter(tokenStream);
	}
}
