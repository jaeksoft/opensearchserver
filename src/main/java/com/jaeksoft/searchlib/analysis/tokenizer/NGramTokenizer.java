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

package com.jaeksoft.searchlib.analysis.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;

public class NGramTokenizer extends TokenizerFactory {

	private int min;
	private int max;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(
				ClassPropertyEnum.MIN_GRAM,
				Integer.toString(org.apache.lucene.analysis.ngram.NGramTokenizer.DEFAULT_MIN_NGRAM_SIZE),
				null, 10, 0);
		addProperty(
				ClassPropertyEnum.MAX_GRAM,

				Integer.toString(org.apache.lucene.analysis.ngram.NGramTokenizer.DEFAULT_MAX_NGRAM_SIZE),
				null, 10, 0);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.MIN_GRAM)
			min = Integer.parseInt(value);
		else if (prop == ClassPropertyEnum.MAX_GRAM)
			max = Integer.parseInt(value);
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new org.apache.lucene.analysis.ngram.NGramTokenizer(reader, min,
				max);
	}

}
