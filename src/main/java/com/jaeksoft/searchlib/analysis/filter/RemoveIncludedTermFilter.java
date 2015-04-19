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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenTerm;

public class RemoveIncludedTermFilter extends FilterFactory {

	public static class RemoveIncludedTokenFilter extends AbstractTermFilter {

		private LinkedHashSet<TokenTerm> tokenList = null;

		private HashSet<Integer> flagsToDelete;

		private final String type;

		private final boolean removeMatchingFlags;

		protected RemoveIncludedTokenFilter(TokenStream input, String type,
				boolean removeMatchingFlags) {
			super(input);
			this.type = StringUtils.isEmpty(type) ? null : type;
			this.removeMatchingFlags = removeMatchingFlags;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			if (tokenList == null) {
				tokenList = new LinkedHashSet<TokenTerm>();
				flagsToDelete = new HashSet<Integer>();
				while (input.incrementToken())
					tokenList.add(new TokenTerm(termAtt, posIncrAtt, offsetAtt,
							typeAtt, flagsAtt));
				List<TokenTerm> deletionList = new ArrayList<TokenTerm>(0);
				for (TokenTerm token : tokenList) {
					if (type != null && !type.equals(token.type))
						continue;
					for (TokenTerm token2 : tokenList) {
						if (type != null && !type.equals(token2.type))
							continue;
						if (token2 != token && token2.term.contains(token.term)) {
							deletionList.add(token);
							if (removeMatchingFlags)
								flagsToDelete.add(token.flags);
							break;
						}
					}
				}
				if (flagsToDelete != null && flagsToDelete.size() > 0)
					for (TokenTerm token : tokenList)
						if (flagsToDelete.contains(token.flags))
							deletionList.add(token);

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

	private String type;

	private boolean removeMatchingFlags;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.TOKEN_TYPE, "shingle", null, 20, 1);
		addProperty(ClassPropertyEnum.REMOVE_MATCHING_FLAGS,
				ClassPropertyEnum.BOOLEAN_LIST[0],
				ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.TOKEN_TYPE)
			type = value;
		else if (prop == ClassPropertyEnum.REMOVE_MATCHING_FLAGS)
			removeMatchingFlags = Boolean.parseBoolean(value);
	}

	public void setProperties(String tokenType, Boolean removeMatchingFlags)
			throws SearchLibException {
		if (tokenType != null)
			getProperty(ClassPropertyEnum.TOKEN_TYPE).setValue(tokenType);
		if (removeMatchingFlags != null)
			getProperty(ClassPropertyEnum.REMOVE_MATCHING_FLAGS).setValue(
					removeMatchingFlags.toString());
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new RemoveIncludedTokenFilter(tokenStream, type,
				removeMatchingFlags);
	}
}
