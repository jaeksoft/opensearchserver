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
import com.jaeksoft.searchlib.analysis.filter.stop.PrefixSuffixFilter;
import com.jaeksoft.searchlib.analysis.stopwords.StopWordsManager;

public class PrefixSuffixStopFilter extends FilterFactory {

	private String[] prefixArray = null;
	private String[] suffixArray = null;
	private String prefixList = null;
	private String suffixList = null;
	private String tokenSeparator = null;
	private StopWordsManager stopWordsManager = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		String[] values = config.getStopWordsManager().getList();
		String value = (values != null && values.length > 0) ? values[0] : null;
		addProperty(ClassPropertyEnum.TOKEN_SEPARATOR, " ", null);
		addProperty(ClassPropertyEnum.PREFIX_FILE_LIST, value, values);
		addProperty(ClassPropertyEnum.SUFFIX_FILE_LIST, value, values);
		stopWordsManager = config.getStopWordsManager();
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.PREFIX_FILE_LIST)
			prefixList = value;
		else if (prop == ClassPropertyEnum.SUFFIX_FILE_LIST)
			suffixList = value;
		else if (prop == ClassPropertyEnum.TOKEN_SEPARATOR)
			tokenSeparator = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		if (prefixArray == null && prefixList != null
				&& prefixList.length() > 0)
			prefixArray = stopWordsManager.getPrefixArray(prefixList,
					tokenSeparator);
		if (suffixArray == null)
			suffixArray = stopWordsManager.getSuffixArray(suffixList,
					tokenSeparator);
		return new PrefixSuffixFilter(tokenStream, prefixArray, suffixArray);
	}
}
