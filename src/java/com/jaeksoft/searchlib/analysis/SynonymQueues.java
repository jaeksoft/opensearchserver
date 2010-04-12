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

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import com.jaeksoft.searchlib.util.ExpressionMap;

public class SynonymQueues {

	private SynonymQueue[] queues;

	private String[] terms;

	private int queueSize;

	private LinkedList<String> insertTermList;

	protected SynonymQueues(TreeMap<Integer, ExpressionMap> expressionMaps) {
		Set<Integer> sizes = expressionMaps.keySet();
		queues = new SynonymQueue[sizes.size()];
		int i = sizes.size();
		terms = new String[i];
		queueSize = 0;
		insertTermList = new LinkedList<String>();
		for (Integer n : sizes)
			queues[--i] = new SynonymQueue(expressionMaps.get(n), terms, n);
	}

	protected String getNextInsertTerm() {
		if (insertTermList.isEmpty())
			return null;
		return insertTermList.removeFirst();
	}

	public void setInsertTerm(SynonymQueue queue, String synonymKey) {
		int l = terms.length - queue.getSize();
		while (l-- != 0)
			insertTermList.add(popToken());
		insertTermList.add(synonymKey);
	}

	protected void addToken(String term) {
		int l = terms.length - 1;
		for (int i = 0; i < l; i++)
			terms[i] = terms[i + 1];
		terms[l] = term;
		if (queueSize < terms.length)
			queueSize++;
	}

	public SynonymQueue isSynonym(StringBuffer synonymKey) {
		for (SynonymQueue queue : queues) {
			String key = queue.findSynonym(queueSize);
			if (key != null) {
				synonymKey.append(key);
				return queue;
			}
		}
		return null;
	}

	public String popToken() {
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i];
			if (term != null) {
				System.out.println("popToken " + i + ": " + term);
				terms[i] = null;
				queueSize--;
				return term;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(' ');
		for (String term : terms) {
			sb.append(term);
			sb.append(' ');
		}
		sb.append(" - Queue size: ");
		sb.append(queueSize);
		return sb.toString();
	}

	public boolean isFull() {
		return queueSize == terms.length;
	}

}
