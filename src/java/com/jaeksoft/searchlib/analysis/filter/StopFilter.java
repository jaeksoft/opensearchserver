/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class StopFilter extends FilterFactory {

	private CharArraySet words;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		String[] values = config.getStopWordsManager().getList();
		String value = (values != null && values.length > 0) ? values[0] : null;
		addProperty(ClassPropertyEnum.FILE, value, values);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop != ClassPropertyEnum.FILE)
			return;
		if (value == null || value.length() == 0)
			return;
		words = config.getStopWordsManager().getWords(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new org.apache.lucene.analysis.StopFilter(false, tokenStream,
				words);
	}
}
