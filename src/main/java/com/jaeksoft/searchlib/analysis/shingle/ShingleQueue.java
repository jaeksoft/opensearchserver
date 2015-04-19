/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.shingle;

import com.jaeksoft.searchlib.analysis.TokenTerm;

public class ShingleQueue {

	private TokenTerm[] tokens;

	private int currentSize;

	private String tokenSeparator;

	private static final String TYPE = "shingle";

	public ShingleQueue(String tokenSeparator, int size) {
		this.tokenSeparator = tokenSeparator;
		tokens = new TokenTerm[size];
		currentSize = 0;
	}

	public final boolean isFull() {
		return currentSize == tokens.length;
	}

	public final String getType() {
		return tokens.length == 1 ? tokens[0].type : TYPE;
	}

	public final int getFlags() {
		return tokens.length == 1 ? tokens[0].flags : 0;
	}

	public final void addToken(TokenTerm shingleToken) {
		tokens[currentSize++] = shingleToken;
	}

	public final String getTerm() {
		StringBuilder sb = new StringBuilder(tokens[0].term);
		for (int i = 1; i < currentSize; i++) {
			sb.append(tokenSeparator);
			sb.append(tokens[i].term);
		}
		return sb.toString();
	}

	protected final int getPositionIncrement() {
		return tokens.length == 1 ? 1 : 0;
	}

	protected final int getStartOffset() {
		int startOffset = Integer.MAX_VALUE;
		for (int i = 0; i < currentSize; i++) {
			int so = tokens[i].start;
			if (so < startOffset)
				startOffset = so;
		}
		return startOffset;
	}

	protected final int getEndOffset() {
		int endOffset = 0;
		for (int i = 0; i < currentSize; i++) {
			int so = tokens[i].end;
			if (so > endOffset)
				endOffset = so;
		}
		return endOffset;
	}

	protected final void pop() {
		currentSize--;
		for (int i = 0; i < currentSize; i++)
			tokens[i] = tokens[i + 1];
		tokens[currentSize] = null;
	}

}
