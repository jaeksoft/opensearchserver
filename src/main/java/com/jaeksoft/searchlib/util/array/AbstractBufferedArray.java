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

public abstract class AbstractBufferedArray {

	protected final int arraySize;

	private int currentArrayNumber;

	private int currentArrayPos;

	private int totalSize;

	private Object finalArray;

	protected AbstractBufferedArray(final int maxSize, final int arraySize) {
		this.arraySize = arraySize;
		buildArrays((maxSize / arraySize) + 1);
		currentArrayPos = arraySize;
		currentArrayNumber = 0;
		totalSize = 0;
		finalArray = null;
	}

	protected abstract void buildArrays(final int arraysNumber);

	protected abstract void newCurrentArray(final int currentArrayNumber,
			final int arraySize);

	final protected int checkBeforeAdd() {
		int pos = currentArrayPos;
		if (currentArrayPos == arraySize) {
			newCurrentArray(currentArrayNumber, arraySize);
			currentArrayNumber++;
			currentArrayPos = 1;
			pos = 0;
		} else
			currentArrayPos++;
		totalSize++;
		return pos;
	}

	public final int getSize() {
		return totalSize;
	}

	protected abstract Object newFinalArray(int size);

	protected abstract Object[] getArrays();

	protected abstract void clear();

	protected Object getFinalArray() {
		if (finalArray != null)
			return finalArray;
		int size = totalSize;
		int buffer = arraySize;
		finalArray = newFinalArray(size);
		int pos = 0;
		for (Object array : getArrays()) {
			if (array == null)
				break;
			if (size < arraySize)
				buffer = size;
			System.arraycopy(array, 0, finalArray, pos, buffer);
			pos += buffer;
			size -= buffer;
		}
		clear();
		return finalArray;
	}
}
