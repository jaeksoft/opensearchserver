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

import java.util.Random;

import org.apache.lucene.search.DocIdSet;

import com.jaeksoft.searchlib.util.NativeOss;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayFactory;

public abstract class BitSetFactory {

	public final static BitSetFactory INSTANCE = NativeOss.loaded() ? new NativeFactory()
			: new JavaFactory();

	public abstract BitSetInterface newInstance(final long numbits);

	public abstract BitSetInterface newInstance(final int numbits);

	public abstract DocIdSet getDocIdSet(BitSetInterface bitSet);

	final static private class NativeFactory extends BitSetFactory {

		@Override
		public BitSetInterface newInstance(long numbits) {
			return new NativeBitSet(numbits);
		}

		@Override
		public BitSetInterface newInstance(int numbits) {
			return new NativeBitSet(numbits);
		}

		@Override
		public DocIdSet getDocIdSet(BitSetInterface bitSet) {
			return new NativeDocIdSet((NativeBitSet) bitSet);
		}

	}

	final static private class JavaFactory extends BitSetFactory {

		@Override
		public BitSetInterface newInstance(long numbits) {
			return new JavaBitSet(numbits);
		}

		@Override
		public BitSetInterface newInstance(int numbits) {
			return new JavaBitSet(numbits);
		}

		@Override
		public DocIdSet getDocIdSet(BitSetInterface bitSet) {
			return ((JavaBitSet) bitSet).getDocIdSet();
		}

	}

	private final static void test(BitSetFactory bitSetFactory,
			int[] randomArray1, int[] randomArray2) {
		System.gc();
		long startTime = System.currentTimeMillis();
		long freemem = Runtime.getRuntime().freeMemory();
		BitSetInterface bitSet1 = bitSetFactory
				.newInstance(randomArray1.length * 4);
		BitSetInterface bitSet2 = bitSetFactory
				.newInstance(randomArray2.length * 4);
		for (int v : randomArray1)
			bitSet1.set(v);
		for (int v : randomArray2)
			bitSet2.set(v);
		bitSet1.cardinality();
		bitSet1.and(bitSet2);
		System.out.println(bitSet2.cardinality());
		IntBufferedArrayFactory.result(bitSet1, startTime, freemem);

	}

	public final static void main(String[] str) {
		final int size = 10000000;

		Random random = new Random(System.currentTimeMillis());
		// Building the index
		long startTime = System.currentTimeMillis();
		long freemem = Runtime.getRuntime().freeMemory();
		int[] randomArray1 = new int[size];
		for (int i = 0; i < size; i++)
			randomArray1[i++] = random.nextInt(size * 4);
		int[] randomArray2 = new int[size];
		for (int i = 0; i < size; i++)
			randomArray2[i++] = random.nextInt(size * 4);
		IntBufferedArrayFactory.result(randomArray1, startTime, freemem);

		test(new JavaFactory(), randomArray1, randomArray2);
		test(new NativeFactory(), randomArray1, randomArray2);
	}
}
