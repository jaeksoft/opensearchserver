/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class SynonymMap {

	private TreeMap<String, List<String>> sourceMap;

	private class SynNode {

		private TreeMap<String, SynNode> map = null;

		private SynNode getWord(String word) {
			if (map == null)
				map = new TreeMap<String, SynNode>();
			SynNode node = map.get(word);
			if (node != null)
				return node;
			return map.put(word, new SynNode());
		}

		private void addWords(String[] words, int pos) {
			if (pos == words.length)
				return;
			getWord(words[pos]).addWords(words, ++pos);
		}

		private void addWords(String[] words) {
			addWords(words, 0);
		}
	}

	private volatile SynNode synMap;

	public SynonymMap(File file) throws FileNotFoundException, IOException {
		sourceMap = new TreeMap<String, List<String>>();
		synMap = null;
		loadFromFile(file);
		updatePerfMap();
	}

	private void loadFromFile(File file) throws FileNotFoundException,
			IOException {
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			String[] keyValue = line.split("[:=]", 2);
			if (keyValue == null)
				continue;
			if (keyValue.length != 2)
				continue;
			String key = keyValue[0].trim();
			String value = keyValue[1];
			String[] terms = value.split(",");
			for (String term : terms)
				addSourceMap(key, term.trim());
		}
		updatePerfMap();
		br.close();
		isr.close();
		fis.close();
	}

	private void updatePerfMap() {
		synchronized (sourceMap) {
			SynNode tn = new SynNode();
			for (String key : sourceMap.keySet()) {
				List<String> termList = sourceMap.get(key);
				if (termList == null || termList.size() == 0)
					continue;
				for (String term : termList) {
					String[] words = term.split("\\s");
					tn.getWord(key).addWords(words);
				}
			}
			synMap = tn;
		}

	}

	private void addSourceMap(String key, String value) {
		if (value == null || value.length() == 0)
			return;
		synchronized (sourceMap) {
			List<String> termList = sourceMap.get(key);
			if (termList == null) {
				termList = new ArrayList<String>(1);
				sourceMap.put(key, termList);
			}
			termList.add(value);
			System.out.println(key + "=>" + value);
		}
	}

	public String[] getSynonyms(String term) {
		System.out.println("getSynonyms");
		List<String> terms = sourceMap.get(term);
		String[] termArray = new String[terms.size()];
		return terms.toArray(termArray);
	}

	public int getSize() {
		return sourceMap.size();
	}

}
