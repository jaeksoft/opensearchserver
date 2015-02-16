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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.opensearchserver.client.common.search.result.VectorPosition;

public class DeduplicateTokenPositionsFilter extends FilterFactory {

	public static class DedupAllTokenPositionsFilter extends AbstractTermFilter {

		private final Map<String, List<VectorPosition>> tokens;

		protected DedupAllTokenPositionsFilter(TokenStream input) {
			super(input);
			this.tokens = new TreeMap<String, List<VectorPosition>>();
		}

		@Override
		public final boolean incrementToken() throws IOException {
			while (input.incrementToken()) {
				String term = termAtt.toString();
				List<VectorPosition> positions = tokens.get(term);
				VectorPosition position = new VectorPosition(
						offsetAtt.startOffset(), offsetAtt.endOffset());
				if (positions == null) {
					positions = new ArrayList<VectorPosition>(1);
					positions.add(position);
					tokens.put(term, positions);
					createToken(term);
					return true;
				}
				positions.add(position);
			}
			return false;
		}
	}

	private Map<String, List<VectorPosition>> lastTokenMap = null;

	@Override
	public TokenStream create(TokenStream tokenStream) {
		DedupAllTokenPositionsFilter tokenFilter = new DedupAllTokenPositionsFilter(
				tokenStream);
		lastTokenMap = tokenFilter.tokens;
		return tokenFilter;
	}

	public Map<String, List<VectorPosition>> getLastTokenMap() {
		return lastTokenMap;
	}
}
