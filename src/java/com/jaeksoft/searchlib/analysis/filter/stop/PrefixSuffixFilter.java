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

package com.jaeksoft.searchlib.analysis.filter.stop;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class PrefixSuffixFilter extends AbstractTermFilter {

	private final String[] prefixArray;
	private final String[] suffixArray;

	public PrefixSuffixFilter(TokenStream input, String[] prefixArray,
			String[] suffixArray) {
		super(input);
		this.prefixArray = prefixArray;
		this.suffixArray = suffixArray;
	}

	private final boolean keepTerm(String term) {
		if (prefixArray != null)
			for (String prefix : prefixArray)
				if (term.startsWith(prefix))
					return false;
		for (String suffix : suffixArray)
			if (term.endsWith(suffix))
				return false;
		return true;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		int skippedPositions = 0;
		for (;;) {
			if (!input.incrementToken())
				return false;
			if (keepTerm(this.getTerm())) {
				posIncrAtt.setPositionIncrement(posIncrAtt
						.getPositionIncrement() + skippedPositions);
				return true;
			}
			skippedPositions += posIncrAtt.getPositionIncrement();
		}
	}

}
