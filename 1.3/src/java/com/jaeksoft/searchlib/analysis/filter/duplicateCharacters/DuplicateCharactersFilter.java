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
package com.jaeksoft.searchlib.analysis.filter.duplicateCharacters;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class DuplicateCharactersFilter extends AbstractTermFilter {

	private final boolean removeLetters;
	private final boolean removeDigits;
	private final boolean removeWhiteSpaces;

	public DuplicateCharactersFilter(TokenStream input, boolean removeLetters,
			boolean removeDigits, boolean removeWhiteSpaces) {
		super(input);
		this.removeLetters = removeLetters;
		this.removeDigits = removeDigits;
		this.removeWhiteSpaces = removeWhiteSpaces;
	}

	private final String removeConsecutiveCharacters(char[] buffer, int length) {
		StringBuffer sb = new StringBuffer();
		char lastChar = 0;
		int i = 0;
		int l = length;
		while (l-- != 0) {
			char currentChar = buffer[i++];
			if (removeLetters)
				if (Character.isLetter(currentChar))
					if (currentChar == lastChar)
						continue;
			if (removeDigits)
				if (Character.isDigit(currentChar))
					if (currentChar == lastChar)
						continue;
			if (removeWhiteSpaces)
				if (Character.isWhitespace(currentChar))
					if (currentChar == lastChar)
						continue;
			sb.append(currentChar);
			lastChar = currentChar;
		}
		// If the size is the same, we don't need to override the term
		if (sb.length() == length)
			return null;
		return sb.toString();
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		if (!input.incrementToken())
			return false;
		String term = removeConsecutiveCharacters(termAtt.termBuffer(),
				termAtt.termLength());
		if (term != null)
			createToken(term);
		return true;
	}

}
