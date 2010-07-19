/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.tokenizer;

public enum TokenizerEnum {

	LetterOrDigitTokenizerFactory(
			"This tokenizer considers each non-digit, non-letter character to be a separator between words"),

	NGramTokenizer("Tokenizes the input into n-grams of the given size(s)."),

	EdgeNGramTokenizer(
			"Create n-grams from the beginning edge or ending edge of a input token."),

	StandardTokenizer(
			"Splits words at punctuation characters, removing punctuation."),

	WhitespaceTokenizer(
			"Splits text into word each time a white space is encountered"),

	ChineseTokenizer("Chinese tokenizer"),

	RussianLetterTokenizer("Russian tokenizer");

	private String description;

	private TokenizerEnum(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	private static String[] tokenizerEnumArray = null;

	public static synchronized String[] getStringArray() {
		if (tokenizerEnumArray != null)
			return tokenizerEnumArray;
		tokenizerEnumArray = new String[TokenizerEnum.values().length];
		int i = 0;
		for (TokenizerEnum te : TokenizerEnum.values())
			tokenizerEnumArray[i++] = te.name();
		return tokenizerEnumArray;
	}
}
