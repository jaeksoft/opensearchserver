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
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.synonym.SynonymMap;
import com.jaeksoft.searchlib.analysis.synonym.SynonymQueue;
import com.jaeksoft.searchlib.analysis.synonym.SynonymTokenFilter;
import com.jaeksoft.searchlib.config.Config;

public class SynonymFilter extends FilterFactory {

	private SynonymMap synonymMap = null;

	private String filePath = null;

	private static TreeMap<File, SynonymMap> synonymMaps = new TreeMap<File, SynonymMap>();

	@Override
	public void setParams(Config config, String packageName, String className,
			Properties properties) throws IOException {
		super.setParams(config, packageName, className, properties);
		filePath = properties.getProperty("file");
		File file = new File(config.getIndexDirectory(), filePath);

		synchronized (synonymMaps) {
			synonymMap = synonymMaps.get(file);
			if (synonymMap == null) {
				synonymMap = new SynonymMap(file);
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
