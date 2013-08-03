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

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hunspell.HunspellDictionary;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.spellcheck.HunspellCache;

public class HunspellStemFilter extends FilterFactory {

	private String affix_path = null;
	private String dict_path = null;
	private boolean ignore_case = false;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.HUNSPELL_AFFIX_PATH, "", null);
		addProperty(ClassPropertyEnum.HUNSPELL_DICT_PATH, "", null);
		addProperty(ClassPropertyEnum.IGNORE_CASE, Boolean.TRUE.toString(),
				ClassPropertyEnum.BOOLEAN_LIST);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {

		if (prop == ClassPropertyEnum.HUNSPELL_AFFIX_PATH) {
			affix_path = value;
		} else if (prop == ClassPropertyEnum.HUNSPELL_DICT_PATH) {
			dict_path = value;
		} else if (prop == ClassPropertyEnum.IGNORE_CASE) {
			ignore_case = Boolean.parseBoolean(value);
		}
	}

	@Override
	public TokenStream create(TokenStream input) throws SearchLibException {
		try {
			HunspellDictionary dict = HunspellCache.getDictionnary(affix_path,
					dict_path, ignore_case);
			return new org.apache.lucene.analysis.hunspell.HunspellStemFilter(
					input, dict);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

}
