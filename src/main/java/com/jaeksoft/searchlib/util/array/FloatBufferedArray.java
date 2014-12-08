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
import java.util.List;

public class FloatBufferedArray implements FloatBufferedArrayInterface {

	private final long maxSize;

	private int initialArraySize;

	private int nextArraySize;

	private final List<float[]> arrays;

	private float[] currentArray;

	private int currentArrayPos;

	private float[] finalArray;

	private int totalSize;

	private FloatBufferedArray(final long maxSize, final int initialArraySize) {
		this.maxSize = maxSize;
		this.initialArraySize = initialArraySize;
		this.nextArraySize = initialArraySize;
		this.arrays = new ArrayList<float[]>();
		this.totalSize = 0;
		newCurrentArray();
	}

	FloatBufferedArray(final long maxSize) {
		this(maxSize, 4096);
	}

	final void newCurrentArray() {
		currentArray = new float[nextArraySize];
		arrays.add(currentArray);
		currentArrayPos = 0;
		if (nextArraySize > maxSize - totalSize)
			nextArraySize = (int) (maxSize - totalSize);
	}

	@Override
	final public void add(final float value) {
		if (currentArrayPos == currentArray.length)
			newCurrentArray();
		currentArray[currentArrayPos++] = value;
		totalSize++;
	}

	@Override
	final public long getSize() {
		return totalSize;
	}

	@Override
	final public float[] getFinalArray() {
		if (finalArray != null)
			return finalArray;
		finalArray = new float[totalSize];
		int sizeLeft = totalSize;
		int buffer;
		int pos = 0;
		for (float[] array : arrays) {
			buffer = array.length;
			if (buffer > sizeLeft)
				buffer = sizeLeft;
			System.arraycopy(array, 0, finalArray, pos, buffer);
			pos += buffer;
			sizeLeft -= buffer;
		}
		clear();
		return finalArray;
	}

	final protected void clear() {
		this.nextArraySize = initialArraySize;
		arrays.clear();
	}

}
