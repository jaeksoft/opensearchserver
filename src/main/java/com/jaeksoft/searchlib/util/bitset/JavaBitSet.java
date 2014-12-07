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

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;

public class JavaBitSet implements BitSetInterface {

	private final OpenBitSet bitSet;

	JavaBitSet(long numBits) {
		this.bitSet = new OpenBitSet(numBits);
	}

	JavaBitSet(int numBits) {
		this.bitSet = new OpenBitSet(numBits);
	}

	private JavaBitSet(OpenBitSet bitSet) {
		this.bitSet = (OpenBitSet) bitSet.clone();
	}

	@Override
	final public BitSetInterface clone() {
		return new JavaBitSet(bitSet);
	}

	@Override
	final public long size() {
		return this.bitSet.size();
	}

	@Override
	final public boolean get(final long bit) {
		return this.bitSet.fastGet(bit);
	}

	@Override
	final public void set(final long bit) {
		this.bitSet.fastSet(bit);
	}

	@Override
	final public void set(final int[] bits) {
		for (int bit : bits)
			this.bitSet.fastSet(bit);
	}

	@Override
	final public void set(final long[] bits) {
		for (long bit : bits)
			bitSet.fastSet(bit);
	}

	@Override
	final public long cardinality() {
		return bitSet.cardinality();
	}

	@Override
	final public void flip(final long startBit, final long endBit) {
		this.bitSet.flip(startBit, endBit);
	}

	@Override
	final public void and(final BitSetInterface bitSet) {
		this.bitSet.and(((JavaBitSet) bitSet).bitSet);
	}

	@Override
	final public void or(final BitSetInterface bitSet) {
		this.bitSet.or(((JavaBitSet) bitSet).bitSet);
	}

	@Override
	final public void clear(final long bit) {
		this.bitSet.fastClear(bit);
	}

	@Override
	final public long nextSetBit(final long index) {
		return this.bitSet.nextSetBit(index);
	}

	final DocIdSet getDocIdSet() {
		return this.bitSet;
	}
}
