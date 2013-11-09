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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.webservice.query.document.DocumentResult.Position;

public class DeduplicateTokenPositionsFilter extends FilterFactory {

	public static class DedupAllTokenPositionsFilter extends AbstractTermFilter {

		private final Map<String, List<Position>> tokens;

		protected DedupAllTokenPositionsFilter(TokenStream input) {
			super(input);
			this.tokens = new TreeMap<String, List<Position>>();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			while (input.incrementToken()) {
				String term = termAtt.toString();
				List<Position> positions = tokens.get(term);
				if (positions == null) {
					positions = new ArrayList<Position>(1);
					positions.add(new Position(offsetAtt));
					tokens.put(term, positions);
					createToken(term);
					return true;
				}
				positions.add(new Position(offsetAtt));
			}
			return false;
		}
	}

	private Map<String, List<Position>> lastTokenMap = null;

	@Override
	public TokenStream create(TokenStream tokenStream) {
		DedupAllTokenPositionsFilter tokenFilter = new DedupAllTokenPositionsFilter(
				tokenStream);
		lastTokenMap = tokenFilter.tokens;
		return tokenFilter;
	}

	public Map<String, List<Position>> getLastTokenMap() {
		return lastTokenMap;
	}
}
