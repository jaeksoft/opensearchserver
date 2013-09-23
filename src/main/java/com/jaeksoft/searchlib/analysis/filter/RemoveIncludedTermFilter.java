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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenTerm;

public class RemoveIncludedTermFilter extends FilterFactory {

	public class RemoveIncludedTokenFilter extends AbstractTermFilter {

		private LinkedHashSet<TokenTerm> tokenList = null;

		protected RemoveIncludedTokenFilter(TokenStream input) {
			super(input);
			addProperty(ClassPropertyEnum.TOKEN_TYPE, "shingle", null);
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (tokenList == null) {
				tokenList = new LinkedHashSet<TokenTerm>();
				while (input.incrementToken())
					tokenList.add(new TokenTerm(termAtt, posIncrAtt, offsetAtt,
							typeAtt));
				List<TokenTerm> deletionList = new ArrayList<TokenTerm>(0);
				for (TokenTerm token : tokenList) {
					for (TokenTerm token2 : tokenList) {
						if (token2 != token && token2.term.contains(token.term)) {
							deletionList.add(token);
							break;
						}
					}
				}
				for (TokenTerm delete : deletionList)
					tokenList.remove(delete);
			}
			if (tokenList.isEmpty())
				return false;
			TokenTerm token = tokenList.iterator().next();
			createToken(token);
			tokenList.remove(token);
			return true;
		}
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new RemoveIncludedTokenFilter(tokenStream);
	}
}
