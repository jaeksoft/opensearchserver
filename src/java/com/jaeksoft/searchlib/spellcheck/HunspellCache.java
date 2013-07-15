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

package com.jaeksoft.searchlib.spellcheck;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class HunspellCache {

	private final static ReadWriteLock rwl = new ReadWriteLock();

	private final static Map<String, HunspellDictionary> dictionaries = new HashMap<String, HunspellDictionary>();

	private final static HunspellDictionary loadDictionnary(String affix_path,
			String dict_path, boolean ignore_case) throws IOException,
			ParseException {

		FileInputStream affixInput = null;
		FileInputStream dictInput = null;
		try {
			affixInput = new FileInputStream(affix_path);
			dictInput = new FileInputStream(dict_path);

			return new HunspellDictionary(affixInput, dictInput,
					Version.LUCENE_36, ignore_case);
		} finally {
			if (affixInput != null)
				IOUtils.closeQuietly(affixInput);
			if (dictInput != null)
				IOUtils.closeQuietly(dictInput);
		}
	}

	private final static String getKey(String affix_path, String dict_path,
			boolean ignore_case) {
		StringBuffer sb = new StringBuffer(affix_path);
		sb.append('/');
		sb.append(dict_path);
		sb.append('/');
		sb.append(ignore_case);
		return sb.toString();
	}

	public final static HunspellDictionary getDictionnary(String affix_path,
			String dict_path, boolean ignore_case) throws IOException,
			ParseException {
		String key = getKey(affix_path, dict_path, ignore_case);
		HunspellDictionary dict = null;
		rwl.r.lock();
		try {
			dict = dictionaries.get(key);
			if (dict != null)
				return dict;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			dict = dictionaries.get(key);
			if (dict != null)
				return dict;
			dict = loadDictionnary(affix_path, dict_path, ignore_case);
			dictionaries.put(key, dict);
			return dict;
		} finally {
			rwl.w.unlock();
		}
	}
}
