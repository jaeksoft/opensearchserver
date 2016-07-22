/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;

public class CustomCharTokenizerFactory extends TokenizerFactory {

	private static final char[] EMTPY_CHAR_ARRAY = {};

	private char[] charArray = EMTPY_CHAR_ARRAY;

	public class CustomCharTokenizer extends CharTokenizer {

		private char[] charArray;

		public CustomCharTokenizer(Reader input, char[] charArray) {
			super(Version.LUCENE_36, input);
			this.charArray = charArray;
		}

		@Override
		final protected boolean isTokenChar(int c) {
			for (char ch : charArray)
				if (ch == c)
					return false;
			return true;
		}
	}

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.CUSTOM_TOKEN_CHARS, " ", null, 10, 0);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value) throws SearchLibException {
		if (prop == ClassPropertyEnum.CUSTOM_TOKEN_CHARS) {
			if (value == null)
				charArray = EMTPY_CHAR_ARRAY;
			else
				charArray = value.toCharArray();
		}
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new CustomCharTokenizer(reader, charArray);
	}

}
