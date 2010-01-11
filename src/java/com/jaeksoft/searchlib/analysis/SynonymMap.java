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

package com.jaeksoft.searchlib.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

public class SynonymMap {

	private class Expression implements Comparable<Expression> {

		private String[] words;

		private Expression(String w) {
			this.words = w.split("\\p{Space}+");
		}

		private Expression(String[] w) {
			this.words = w;
		}

		private int getSize() {
			return words.length;
		}

		@Override
		public int compareTo(Expression o) {
			int i = 0;
			for (String word : words) {
				if (i >= o.words.length)
					return -1;
				int n;
				if ((n = word.compareToIgnoreCase(o.words[i])) != 0)
					return n;
				i++;
			}
			return 0;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append('_');
			boolean space = false;
			for (String word : words) {
				if (space)
					sb.append('_');
				else
					space = true;
				sb.append(word);
			}
			sb.append('_');
			return sb.toString();
		}

	}

	protected class ExpressionMap {

		TreeMap<Expression, String> map;

		private ExpressionMap() {
			map = new TreeMap<Expression, String>();
		}

		private void add(Expression words, Expression key) {
			map.put(words, key.toString());
		}

		protected String find(String[] words) {
			return map.get(new Expression(words));
		}
	}

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
		Expression expKey = new Expression(key);
		Expression expression = new Expression(words);
		int i = expression.getSize();
		ExpressionMap expressionMap = expressionMaps.get(i);
		if (expressionMap == null) {
			expressionMap = new ExpressionMap();
			expressionMaps.put(i, expressionMap);
		}
		expressionMap.add(expression, expKey);
		// System.out.println(expKey + " => " + expression);
	}

	public int getSize() {
		return size;
	}

	public SynonymQueues getSynonymQueues() {
		return new SynonymQueues(expressionMaps);
	}
}
