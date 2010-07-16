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

package com.jaeksoft.searchlib.analysis.synonym;

import com.jaeksoft.searchlib.util.Expression;
import com.jaeksoft.searchlib.util.ExpressionMap;

public class SynonymQueue {

	private Expression expressionKey;

	private ExpressionMap expressionMap;

	private SynonymToken[] tokens;

	private int queueSize;

	protected SynonymQueue(ExpressionMap expressionMap, int size) {
		this.expressionMap = expressionMap;
		this.tokens = new SynonymToken[size];
		this.expressionKey = new Expression(tokens);
		this.queueSize = 0;
	}

	protected String findSynonym() {
		if (!isFull())
			return null;
		return expressionMap.find(expressionKey);
	}

	protected void clean() {
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = null;
		queueSize = 0;
	}

	protected void addToken(SynonymToken token) {
		int l = tokens.length - 1;
		for (int i = 0; i < l; i++)
			tokens[i] = tokens[i + 1];
		tokens[l] = token;
		if (queueSize < tokens.length)
			queueSize++;
	}

	protected SynonymToken popToken() {
		for (int i = 0; i < tokens.length; i++) {
			SynonymToken token = tokens[i];
			if (token != null) {
				tokens[i] = null;
				queueSize--;
				return token;
			}
		}
		return null;
	}

	protected boolean isFull() {
		return queueSize == tokens.length;
	}

	protected int getPositionIncrement() {
		int pos = 0;
		for (SynonymToken token : tokens)
			if (token != null)
				pos += token.getPositionIncrement();
		return 0;
	}

	protected int getStartOffset() {
		int startOffset = Integer.MAX_VALUE;
		for (SynonymToken token : tokens) {
			if (token != null) {
				int so = token.getStartOffset();
				if (so < startOffset)
					startOffset = so;
			}
		}
		return startOffset;
	}

	protected int getEndOffset() {
		int endOffset = 0;
		for (SynonymToken token : tokens) {
			if (token != null) {
				int so = token.getEndOffset();
				if (so > endOffset)
					endOffset = so;
			}
		}
		return endOffset;
	}

}
