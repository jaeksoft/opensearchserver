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

public abstract class BitSetFactory {

	public final static BitSetFactory INSTANCE;

	static {
		BitSetFactory bsf;
		try {
			System.loadLibrary("nativeoss");
			bsf = new NativeFactory();
		} catch (Throwable t) {
			System.err.println("No nativeoss library: " + t.getMessage());
			bsf = new JavaFactory();
		}
		INSTANCE = bsf;
	}

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

}
