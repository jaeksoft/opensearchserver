/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExpressionMap {

	TreeMap<String, String[]> map;

	public ExpressionMap() {
		map = new TreeMap<String, String[]>();
	}

	public void add(String key, String[] words) {
		String[] oldWords = map.get(key);
		Set<String> wordSet = new TreeSet<String>();
		if (oldWords != null) {
			for (String word : oldWords)
				if (!word.equals(key))
					wordSet.add(word);
		}
		for (String word : words)
			if (!word.equals(key))
				wordSet.add(word);
		words = new String[wordSet.size()];
		wordSet.toArray(words);
		map.put(key, words);
	}

	public String[] find(String key) {
		return map.get(key);
	}
}
