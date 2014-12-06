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

public class NativeBitSet implements BitSetInterface {

	public native NativeBitSet init(long numbits);

	public native NativeBitSet init(int numbits);

	@Override
	public native long size();

	@Override
	public native void set(int bit);

	@Override
	public native void set(long bit);

	@Override
	public native boolean get(long bit);

	@Override
	public native boolean get(int bit);

	@Override
	public native BitSetInterface clone();

	@Override
	public native void set(int[] bits);

	@Override
	public native void set(long[] bits);

	@Override
	public native long cardinality();

	@Override
	public native void flip(long from, long to);

	@Override
	public native void and(BitSetInterface bitSet);

	@Override
	public native void or(BitSetInterface bitSet);

	@Override
	public native void clear(long bit);

	@Override
	public native void clear(int bit);

	@Override
	public native long nextSetBit(long index);

	@Override
	public native int nextSetBit(int index);

}
