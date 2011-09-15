/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.jaeksoft.searchlib.util.Expression;
import com.jaeksoft.searchlib.util.ExpressionMap;
import com.jaeksoft.searchlib.util.ExpressionToken;

public class SynonymMap {

	private TreeMap<Integer, ExpressionMap> expressionMaps;

	private int size;

	public SynonymMap(File file) throws FileNotFoundException, IOException {
		size = 0;
		expressionMaps = new TreeMap<Integer, ExpressionMap>();
		loadFromFile(file);
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
			addSourceMap(key, key);
			for (String term : terms)
				addSourceMap(key, term.trim());
		}
		br.close();
		isr.close();
		fis.close();
	}

	private void addSourceMap(String key, String words) {
		if (words == null || words.length() == 0)
			return;
		size++;
		Expression expKey = new Expression(ExpressionToken.createArray(key));
		Expression expression = new Expression(ExpressionToken
				.createArray(words));
		int i = expression.getSize();
		ExpressionMap expressionMap = expressionMaps.get(i);
		if (expressionMap == null) {
			expressionMap = new ExpressionMap();
			expressionMaps.put(i, expressionMap);
		}
		expressionMap.add(expression, expKey);
	}

	public int getSize() {
		return size;
	}

	public List<SynonymQueue> getSynonymQueues() {
		Set<Integer> sizes = expressionMaps.keySet();
		List<SynonymQueue> queues = new ArrayList<SynonymQueue>();
		for (Integer n : sizes)
			queues.add(new SynonymQueue(expressionMaps.get(n), n));
		return queues;
	}
}
