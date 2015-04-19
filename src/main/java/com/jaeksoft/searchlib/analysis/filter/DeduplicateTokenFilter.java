/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.analysis.FilterFactory;

public class DeduplicateTokenFilter extends FilterFactory {

	public static class DedupAllTokenFilter extends AbstractTermFilter {

		private final Set<String> tokens;

		protected DedupAllTokenFilter(TokenStream input) {
			super(input);
			tokens = new TreeSet<String>();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			while (input.incrementToken()) {
				String term = termAtt.toString();
				if (tokens.contains(term))
					continue;
				tokens.add(term);
				createToken(term);
				return true;
			}
			return false;
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new DedupAllTokenFilter(tokenStream);
	}
}
