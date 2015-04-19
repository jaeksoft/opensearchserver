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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.filter.duplicateCharacters.DuplicateCharactersFilter;

public class RemoveDuplicateCharactersFilter extends FilterFactory {

	private boolean removeDuplicateLetters = true;
	private boolean removeDuplicateDigits = true;
	private boolean removeDuplicateWhiteSpaces = true;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.REMOVE_DUPLICATE_LETTERS,
				Boolean.TRUE.toString(), ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
		addProperty(ClassPropertyEnum.REMOVE_DUPLICATE_DIGITS,
				Boolean.TRUE.toString(), ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
		addProperty(ClassPropertyEnum.REMOVE_DUPLICATE_WHITESPACES,
				Boolean.TRUE.toString(), ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.REMOVE_DUPLICATE_LETTERS)
			removeDuplicateLetters = Boolean.parseBoolean(value);
		else if (prop == ClassPropertyEnum.REMOVE_DUPLICATE_DIGITS)
			removeDuplicateDigits = Boolean.parseBoolean(value);
		else if (prop == ClassPropertyEnum.REMOVE_DUPLICATE_WHITESPACES)
			removeDuplicateWhiteSpaces = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new DuplicateCharactersFilter(tokenStream,
				removeDuplicateLetters, removeDuplicateDigits,
				removeDuplicateWhiteSpaces);
	}

}
