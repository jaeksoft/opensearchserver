/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class SynonymTokenFilter extends TokenFilter {

	public static final String TOKEN_TYPE = "SYNONYM";

	private String[] synonyms = null;
	private int index = 0;
	private Token current = null;
	private SynonymMap synonymMap = null;

	protected SynonymTokenFilter(TokenStream input, SynonymMap synonymMap) {
		super(input);
		this.synonymMap = synonymMap;
	}

	private final Token createToken(String synonym, Token current,
			final Token reusableToken) {
		reusableToken.reinit(current, synonym);
		reusableToken.setTermBuffer(synonym);
		reusableToken.setType(TOKEN_TYPE);
		reusableToken.setPositionIncrement(0);
		return reusableToken;
	}

	@Override
	public Token next(Token reusableToken) throws IOException {
		while (index < synonyms.length) { // pop from stack
			Token nextToken = createToken(synonyms[index++], current,
					reusableToken);
			if (nextToken != null)
				return nextToken;
		}

		Token nextToken = input.next(reusableToken);
		if (nextToken == null)
			return null; // EOS; iterator exhausted

		synonyms = synonymMap.getSynonyms(nextToken.term()); // push onto stack
		index = 0;
		current = (Token) nextToken.clone();
		return nextToken;
	}

}
