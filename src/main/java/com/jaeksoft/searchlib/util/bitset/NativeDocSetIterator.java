/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util.bitset;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;

public class NativeDocSetIterator extends DocIdSetIterator {

	private final NativeBitSet bitSet;
	private int currentIndex;

	NativeDocSetIterator(NativeBitSet bitSet) {
		this.bitSet = bitSet;
		this.currentIndex = -1;
	}

	@Override
	public int docID() {
		return currentIndex;
	}

	@Override
	public int nextDoc() {
		currentIndex = bitSet.nextSetBit(currentIndex + 1);
		if (currentIndex == -1)
			currentIndex = NO_MORE_DOCS;
		return currentIndex;
	}

	@Override
	public int advance(int target) throws IOException {
		while ((currentIndex = nextDoc()) < target)
			;
		return currentIndex;
	}
}
