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
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer.Side;

import com.jaeksoft.searchlib.SearchLibException;

public class EdgeNGramTokenizer extends TokenizerFactory {

	private int min;
	private int max;
	private Side side;

	@Override
	public void setProperty(String key, String value) throws SearchLibException {
		super.setProperty(key, value);
		if ("min_gram".equals(key))
			min = Integer.parseInt(value);
		else if ("max_gram".equals(key))
			max = Integer.parseInt(value);
		else if ("side".equals(key))
			side = Side.getSide(value);
	}

	private final static String[] PROPLIST = { "min_gram", "max_gram", "side" };

	@Override
	public String[] getPropertyKeyList() {
		return PROPLIST;
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new org.apache.lucene.analysis.ngram.EdgeNGramTokenizer(reader,
				side, min, max);
	}
}
