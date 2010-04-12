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

import com.jaeksoft.searchlib.util.Expression;
import com.jaeksoft.searchlib.util.ExpressionMap;

class SynonymQueue {

	private Expression expressionKey;

	private ExpressionMap expressionMap;

	private int size;

	protected SynonymQueue(ExpressionMap expressionMap, String[] tokens,
			int size) {
		this.expressionMap = expressionMap;
		this.expressionKey = new Expression(tokens, tokens.length - size);
		this.size = size;
	}

	protected int getSize() {
		return size;
	}

	protected String findSynonym(int queueSize) {
		if (queueSize < size)
			return null;
		return expressionMap.find(expressionKey);
	}

}
