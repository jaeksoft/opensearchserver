/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.IOException;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

public class RoaringDocIdSet extends DocIdSet {

	private final RoaringBitmap bitSet;

	public RoaringDocIdSet(RoaringBitmap bitSet) {
		this.bitSet = bitSet;
	}

	@Override
	public DocIdSetIterator iterator() throws IOException {
		return new RoaringDocSetIterator();
	}

	public class RoaringDocSetIterator extends DocIdSetIterator {

		private final IntIterator intIterator;
		private int currentIndex;

		private RoaringDocSetIterator() {
			intIterator = bitSet.getIntIterator();
			this.currentIndex = -1;
		}

		@Override
		final public int docID() {
			return currentIndex;
		}

		@Override
		final public int nextDoc() {
			if (!intIterator.hasNext())
				currentIndex = NO_MORE_DOCS;
			else
				currentIndex = intIterator.next();
			return currentIndex;
		}

		@Override
		final public int advance(int target) throws IOException {
			while ((currentIndex = nextDoc()) < target)
				;
			return currentIndex;
		}
	}

}
