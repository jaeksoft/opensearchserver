/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util.array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IntBufferedArrayList {

	private final int arraySize;

	private final List<int[]> arrays;

	private int[] currentArray;

	private int currentPos;

	private int size;

	public IntBufferedArrayList(final int arraySize) {
		this.arraySize = arraySize;
		this.arrays = new ArrayList<int[]>();
		this.size = 0;
		newArray();
	}

	private final void newArray() {
		currentArray = new int[arraySize];
		arrays.add(currentArray);
		currentPos = 0;
	}

	public final void add(final int value) {
		currentArray[currentPos] = value;
		size++;
		if (++currentPos == arraySize)
			newArray();
	}

	public final void add(final int... values) {
		for (int value : values)
			add(value);
	}

	public final int getSize() {
		return size;
	}

	public final void clear() {
		if (size == 0)
			return;
		arrays.clear();
		newArray();
		size = 0;
	}

	public Collection<int[]> getArrays() {
		return arrays;
	}

	public int getCurrentSize() {
		return currentPos;
	}
}
