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

package com.jaeksoft.searchlib.analysis.synonym;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.jaeksoft.searchlib.util.ExpressionMap;

public class SynonymMap {

	private ExpressionMap expressionMap;
	private int size;

	public SynonymMap(File file) throws FileNotFoundException, IOException {
		size = 0;
		expressionMap = new ExpressionMap();
		loadFromFile(file);
	}

	private static final String[] splitTerms(String line) {
		String[] terms = line.split("[:=,]");
		int i = 0;
		for (String term : terms)
			terms[i++] = term.trim();
		return terms;
	}

	private void loadFromFile(File file) throws FileNotFoundException,
			IOException {
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			String[] terms = splitTerms(line);
			for (String key : terms)
				expressionMap.add(key, terms);
			size++;
		}
		br.close();
		isr.close();
		fis.close();
	}

	public int getSize() {
		return size;
	}

	public final String[] getSynonyms(String term) {
		return expressionMap.find(term);
	}
}
