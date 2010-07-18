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

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;

import com.jaeksoft.searchlib.SearchLibException;

public class NGramTokenizer extends TokenizerFactory {

	private int min;
	private int max;

	@Override
	public void setProperty(String key, String value) throws SearchLibException {
		super.setProperty(key, value);
		if ("minGram".equals(key))
			min = Integer.parseInt(value);
		else if ("maxGram".equals(key))
			max = Integer.parseInt(value);
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new org.apache.lucene.analysis.ngram.NGramTokenizer(reader, min,
				max);
	}

	private final static String[] PROPLIST = { "minGram", "maxGram" };

	@Override
	public String[] getPropertyKeyList() {
		return PROPLIST;
	}

}
