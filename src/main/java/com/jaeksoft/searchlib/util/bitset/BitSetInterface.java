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


public interface BitSetInterface {

	long size();

	void set(int doc);

	void set(long doc);

	void set(int[] srcIds);

	void set(long[] srcIds);

	boolean get(long doc);

	boolean get(int doc);

	BitSetInterface clone();

	long cardinality();

	void flip(long from, long to);

	void and(BitSetInterface bitSet);

	void or(BitSetInterface bitSet);

	void clear(long bit);

	void clear(int bit);

	long nextSetBit(long index);

	int nextSetBit(int index);
}
