/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter.stop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.util.IOUtils;

public class WordArray {

	final protected TreeSet<String> wordSet;

	final protected boolean ignoreCase;

	public WordArray(WordArray wordArray, boolean ignoreCase) {
		wordSet = (wordArray != null) ? wordArray.wordSet
				: new TreeSet<String>();
		this.ignoreCase = ignoreCase;
	}

	public WordArray(File file, boolean ignoreCase) throws IOException {
		this((WordArray) null, ignoreCase);
		buildList(file);
	}

	private final void buildList(File file) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				if (ignoreCase)
					line = line.toLowerCase();
				wordSet.add(line);
			}
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
		}
	}

	public Set<String> getWordSet() {
		return wordSet;
	}

	public boolean match(String term) {
		return wordSet.contains(term);
	}

}
