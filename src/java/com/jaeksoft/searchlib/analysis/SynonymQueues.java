/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.jaeksoft.searchlib.analysis.SynonymMap.ExpressionMap;

public class SynonymQueues {

	private class SynonymQueue {

		private String[] tokens;

		private int size;

		private ExpressionMap expressionMap;

		private SynonymQueue(ExpressionMap expressionMap, int size) {
			this.expressionMap = expressionMap;
			this.size = size;
			tokens = new String[size];
		}

		private void addToken(String token) {
			int l = tokens.length - 1;
			for (int i = 0; i < l; i++)
				tokens[i] = tokens[i + 1];
			tokens[l] = token;
			if (size > 0)
				size--;
		}

		private String findSynonym() {
			if (size != 0)
				return null;
			return expressionMap.find(tokens);
		}

		private void reset() {
			for (int i = 0; i < tokens.length; i++)
				tokens[i] = null;
			size = tokens.length;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for (String token : tokens) {
				sb.append(i++);
				sb.append("=>");
				sb.append(token);
				sb.append(" (");
				sb.append(size);
				sb.append(')');
			}
			return sb.toString();
		}
	}

	private SynonymQueue[] queues;

	protected SynonymQueues(TreeMap<Integer, ExpressionMap> expressionMaps) {
		Set<Integer> sizes = expressionMaps.keySet();
		queues = new SynonymQueue[sizes.size()];
		int i = 0;
		for (Integer n : sizes)
			queues[i++] = new SynonymQueue(expressionMaps.get(n), n);
	}

	public List<String> getSynonym(String token) {
		// System.out.println("Add " + token);
		List<String> synonymList = new ArrayList<String>();
		for (SynonymQueue queue : queues) {
			queue.addToken(token);
			// System.out.println(queue);
			String synonym = queue.findSynonym();
			if (synonym != null) {
				synonymList.add(synonym);
				queue.reset();
			}
		}
		return synonymList.size() == 0 ? null : synonymList;
	}
}
