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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SynonymMap {

	private TreeMap<String, List<String>> sourceMap;
	private volatile TreeMap<String, String[]> perfMap;

	public SynonymMap(File file) throws FileNotFoundException, IOException {
		sourceMap = new TreeMap<String, List<String>>();
		perfMap = null;
		loadFromFile(file);
		updatePerfMap();
	}

	private void loadFromFile(File file) throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		props.load(new FileReader(file));
		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			String value = props.getProperty(key);
			String[] terms = value.split(",");
			for (String term : terms)
				addSourceMap(key, term.trim());
		}
		updatePerfMap();
	}

	private void updatePerfMap() {
		synchronized (sourceMap) {
			TreeMap<String, String[]> spm = new TreeMap<String, String[]>();
			Set<String> termSet = new TreeSet<String>();
			for (String key : sourceMap.keySet()) {
				List<String> termList = sourceMap.get(key);
				if (termList == null || termList.size() == 0)
					continue;
				termSet.clear();
				for (String term : termList) {
					String[] words = term.split("\\s*");
					for (String word : words)
						termSet.add(word);
				}
				String[] termArray = new String[termList.size()];
				termSet.toArray(termArray);
				spm.put(key, termArray);
			}
			perfMap = spm;
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
		}
	}

	public String[] getSynonyms(String term) {
		return perfMap.get(term);
	}

}
