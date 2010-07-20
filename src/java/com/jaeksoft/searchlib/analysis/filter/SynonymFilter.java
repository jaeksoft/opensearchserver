/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.synonym.SynonymMap;
import com.jaeksoft.searchlib.analysis.synonym.SynonymQueue;
import com.jaeksoft.searchlib.analysis.synonym.SynonymTokenFilter;

public class SynonymFilter extends FilterFactory {

	private SynonymMap synonymMap = null;

	private static TreeMap<File, SynonymMap> synonymMaps = new TreeMap<File, SynonymMap>();

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.FILE, null, config.getStopWordsManager()
				.getList());
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop != ClassPropertyEnum.FILE)
			return;
		if (value == null || value.length() == 0)
			return;
		File file = new File(config.getIndexDirectory(), value);
		if (!file.exists() || !file.isFile())
			throw new SearchLibException("File not found (" + value + ")");
		synchronized (synonymMaps) {
			synonymMap = synonymMaps.get(file);
			if (synonymMap == null) {
				try {
					synonymMap = new SynonymMap(file);
				} catch (FileNotFoundException e) {
					throw new SearchLibException(e);
				} catch (IOException e) {
					throw new SearchLibException(e);
				}
				synonymMaps.put(file, synonymMap);
			}
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		for (SynonymQueue queue : synonymMap.getSynonymQueues())
			tokenStream = new SynonymTokenFilter(tokenStream, queue);
		return tokenStream;
	}

}
