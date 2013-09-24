/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.shingle;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.TokenTerm;
import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class ShingleTokenFilter extends AbstractTermFilter {

	private ShingleQueue[] shingles;

	public ShingleTokenFilter(TokenStream tokenStream, String tokenSeparator,
			int minShingleSize, int maxShingleSize) {
		super(tokenStream);
		shingles = new ShingleQueue[maxShingleSize - minShingleSize + 1];
		for (int i = 0; i < shingles.length; i++)
			shingles[i] = new ShingleQueue(tokenSeparator, maxShingleSize - i);
	}

	private ShingleQueue isToken() {
		for (ShingleQueue shingle : shingles)
			if (shingle.isFull())
				return shingle;
		return null;
	}

	private final boolean createToken(ShingleQueue shingle) {
		if (shingle.getTerm() == null)
			return false;
		createToken(shingle.getTerm(), shingle.getPositionIncrement(),
				shingle.getStartOffset(), shingle.getEndOffset(),
				shingle.getType(), shingle.getFlags());
		shingle.pop();
		return true;
	}

	private final void addToken(TokenTerm token) {
		for (ShingleQueue shingle : shingles)
			shingle.addToken(token);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		for (;;) {
			ShingleQueue shingle = isToken();
			if (shingle != null)
				return createToken(shingle);
			if (!input.incrementToken())
				return false;
			addToken(new TokenTerm(termAtt, posIncrAtt, offsetAtt, typeAtt,
					flagsAtt));
		}
	}

}
