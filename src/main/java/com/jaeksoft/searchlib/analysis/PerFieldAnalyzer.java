/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;

public class PerFieldAnalyzer extends AbstractAnalyzer {

	private final Map<String, CompiledAnalyzer> analyzerMap;

	public PerFieldAnalyzer(Map<String, CompiledAnalyzer> analyzerMap) {
		this.analyzerMap = analyzerMap;
	}

	public final CompiledAnalyzer getCompiledAnalyzer(String fieldName) {
		return analyzerMap.get(fieldName);
	}

	@Override
	public final TokenStream tokenStream(final String fieldName,
			final Reader reader) {
		CompiledAnalyzer analyzer = analyzerMap.get(fieldName);
		if (analyzer == null)
			return getKeywordAnalyzer().tokenStream(fieldName, reader);
		return analyzer.tokenStream(fieldName, reader);
	}

	public KeywordAnalyzer getKeywordAnalyzer() {
		return new KeywordAnalyzer();
	}

}
