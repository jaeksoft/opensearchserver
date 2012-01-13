/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.stopwords;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.stop.PrefixArray;
import com.jaeksoft.searchlib.analysis.filter.stop.SuffixArray;
import com.jaeksoft.searchlib.analysis.filter.stop.WordArray;
import com.jaeksoft.searchlib.config.Config;

public class StopWordsManager extends AbstractDirectoryManager {

	private Map<String, WordArray> wordArrayMap;
	private Map<String, PrefixArray> prefixArrayMap;
	private Map<String, SuffixArray> suffixArrayMap;

	public StopWordsManager(Config config, File directory) {
		super(config, directory);
		wordArrayMap = new TreeMap<String, WordArray>();
		prefixArrayMap = new TreeMap<String, PrefixArray>();
		suffixArrayMap = new TreeMap<String, SuffixArray>();
	}

	private final static String getListKey(String listname,
			String tokenSeparator, boolean ignoreCase) {
		return listname + "||" + tokenSeparator + "||" + ignoreCase;
	}

	private final WordArray getOrCreateWordArray(String listName,
			boolean ignoreCase) throws IOException {
		String listKey = getListKey(listName, null, ignoreCase);
		WordArray wordArray = wordArrayMap.get(listKey);
		if (wordArray != null)
			return wordArray;
		wordArray = new WordArray(getFile(listName), ignoreCase);
		wordArrayMap.put(listKey, wordArray);
		return wordArray;
	}

	public final WordArray getWordArray(String listName, boolean ignoreCase) {
		String listKey = getListKey(listName, null, ignoreCase);
		rwl.r.lock();
		try {
			WordArray wordArray = wordArrayMap.get(listKey);
			if (wordArray != null)
				return wordArray;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			return getOrCreateWordArray(listName, ignoreCase);
		} catch (IOException e) {
			Logging.error(e);
			return null;
		} finally {
			rwl.w.unlock();
		}
	}

	public PrefixArray getPrefixArray(String listName, String tokenSeparator,
			boolean ignoreCase) {
		String listKey = getListKey(listName, tokenSeparator, ignoreCase);
		rwl.r.lock();
		try {
			PrefixArray prefixArray = prefixArrayMap.get(listKey);
			if (prefixArray != null)
				return prefixArray;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			PrefixArray prefixArray = prefixArrayMap.get(listKey);
			if (prefixArray != null)
				return prefixArray;
			WordArray wordArray = getOrCreateWordArray(listName, ignoreCase);
			prefixArray = new PrefixArray(wordArray, ignoreCase, tokenSeparator);
			prefixArrayMap.put(listKey, prefixArray);
			return prefixArray;
		} catch (IOException e) {
			Logging.error(e);
			return null;
		} finally {
			rwl.w.unlock();
		}
	}

	public SuffixArray getSuffixArray(String listName, String tokenSeparator,
			boolean ignoreCase) {
		String listKey = getListKey(listName, tokenSeparator, ignoreCase);
		rwl.r.lock();
		try {
			SuffixArray suffixArray = suffixArrayMap.get(listKey);
			if (suffixArray != null)
				return suffixArray;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			SuffixArray suffixArray = suffixArrayMap.get(listKey);
			if (suffixArray != null)
				return suffixArray;
			WordArray wordArray = getOrCreateWordArray(listName, ignoreCase);
			suffixArray = new SuffixArray(wordArray, ignoreCase, tokenSeparator);
			suffixArrayMap.put(listKey, suffixArray);
			return suffixArray;
		} catch (IOException e) {
			Logging.error(e);
			return null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void delete(String name) {
		rwl.w.lock();
		try {
			super.delete(name);
			wordArrayMap.clear();
			prefixArrayMap.clear();
			suffixArrayMap.clear();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void saveContent(String name, String content) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			super.saveContent(name, content);
			wordArrayMap.clear();
			prefixArrayMap.clear();
			suffixArrayMap.clear();
		} finally {
			rwl.w.unlock();
		}
	}
}
