/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.analysis.filter.stop.StopWordFilter;
import com.jaeksoft.searchlib.analysis.filter.stop.WordArray;
import com.jaeksoft.searchlib.analysis.stopwords.StopWordsManager;

public class StopFilter extends FilterFactory {

	private String wordList = null;
	private boolean ignoreCase = false;
	private StopWordsManager stopWordsManager = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		String[] values = config.getStopWordsManager().getList(false);
		String value = (values != null && values.length > 0) ? values[0] : null;
		addProperty(ClassPropertyEnum.FILE_LIST, value, values, 0, 0);
		addProperty(ClassPropertyEnum.IGNORE_CASE, Boolean.FALSE.toString(),
				ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
		stopWordsManager = config.getStopWordsManager();
	}

	public void setProperties(String wordList, Boolean ignoreCase)
			throws SearchLibException {
		if (wordList != null)
			getProperty(ClassPropertyEnum.FILE_LIST).setValue(wordList);
		if (ignoreCase != null)
			getProperty(ClassPropertyEnum.IGNORE_CASE).setValue(
					ignoreCase.toString());
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.FILE_LIST)
			wordList = value;
		else if (prop == ClassPropertyEnum.IGNORE_CASE)
			ignoreCase = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		WordArray wordArray = null;
		if (wordList != null && wordList.length() > 0)
			wordArray = stopWordsManager.getWordArray(wordList, ignoreCase);
		return new StopWordFilter(tokenStream, wordArray, ignoreCase);
	}
}
