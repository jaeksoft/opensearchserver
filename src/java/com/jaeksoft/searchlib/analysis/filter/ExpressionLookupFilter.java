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

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.filter.stop.WordArray;

public class ExpressionLookupFilter extends FilterFactory {

	private WordArray words = null;
	private boolean ignoreCase = false;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		String[] values = config.getStopWordsManager().getList();
		String value = (values != null && values.length > 0) ? values[0] : null;
		addProperty(ClassPropertyEnum.FILE_LIST, value, values);
		addProperty(ClassPropertyEnum.IGNORE_CASE, Boolean.FALSE.toString(),
				ClassPropertyEnum.BOOLEAN_LIST);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop != ClassPropertyEnum.FILE_LIST)
			return;
		if (value == null || value.length() == 0)
			return;
		words = config.getStopWordsManager().getWordArray(value, ignoreCase);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		if (words == null)
			return tokenStream;
		return new ExpressionLookupTokenFilter(tokenStream, words);
	}

	public class ExpressionLookupTokenFilter extends AbstractTermFilter {

		private WordArray words = null;

		public ExpressionLookupTokenFilter(TokenStream input, WordArray words) {
			super(input);
			this.words = words;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			while (input.incrementToken()) {
				if (words.match(termAtt.toString()))
					return true;
			}
			return false;
		}
	}

}
