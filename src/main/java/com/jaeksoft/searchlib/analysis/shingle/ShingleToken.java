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

public class ShingleToken {

	public final String term;
	public final int positionIncrement;
	public final int startOffset;
	public final int endOffset;
	public final String type;

	protected ShingleToken(String term, int posInc, int start, int end,
			String type) {
		this.term = term;
		this.positionIncrement = posInc;
		this.startOffset = start;
		this.endOffset = end;
		this.type = type;
	}

	public final int getPositionIncrement() {
		return positionIncrement;
	}

	public final int getStartOffset() {
		return startOffset;
	}

	public final int getEndOffset() {
		return endOffset;
	}

	public final String getTerm() {
		return term;
	}
}
