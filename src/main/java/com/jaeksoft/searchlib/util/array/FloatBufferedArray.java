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

import it.unimi.dsi.fastutil.floats.FloatArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FloatBufferedArray {

	private final int maxSize;

	private int initialArraySize;

	private int nextArraySize;

	private final List<float[]> arrays;

	private float[] currentArray;

	private int currentArrayPos;

	private float[] finalArray;

	private int totalSize;

	public FloatBufferedArray(final int maxSize, final int initialArraySize) {
		this.maxSize = maxSize;
		this.initialArraySize = initialArraySize;
		this.nextArraySize = initialArraySize;
		this.arrays = new ArrayList<float[]>();
		this.totalSize = 0;
		newCurrentArray();
	}

	public FloatBufferedArray(final int maxSize) {
		this(maxSize, 4096);
	}

	final void newCurrentArray() {
		currentArray = new float[nextArraySize];
		arrays.add(currentArray);
		currentArrayPos = 0;
		if (nextArraySize > maxSize - totalSize)
			nextArraySize = maxSize - totalSize;
	}

	final public void add(final float value) {
		if (currentArrayPos == currentArray.length)
			newCurrentArray();
		currentArray[currentArrayPos++] = value;
		totalSize++;
	}

	final public int getSize() {
		return totalSize;
	}

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

	public final static void main(String[] str) {
		final int size = 1000000;

		Random random = new Random(System.currentTimeMillis());
		// Building the index
		long startTime = System.currentTimeMillis();
		long freemem = Runtime.getRuntime().freeMemory();
		float[] randomArray = new float[size];
		for (int i = 0; i < size; i++)
			randomArray[i++] = random.nextFloat();
		IntBufferedArray.result(randomArray, startTime, freemem);

		// Testing Native Array reduced size
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		float[] nativeArray1 = new float[size];
		int i = 0;
		for (float v : randomArray)
			nativeArray1[i++] = v;
		IntBufferedArray.result(nativeArray1, startTime, freemem);

		// Testing Native Array
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		float[] nativeArray = new float[size * 4];
		i = 0;
		for (float v : randomArray)
			nativeArray[i++] = v;
		IntBufferedArray.result(nativeArray, startTime, freemem);

		// Testing FastUTIL
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		FloatArrayList fastUtilArray = new FloatArrayList(size * 4);
		for (float v : randomArray)
			fastUtilArray.add(v);
		fastUtilArray.toFloatArray();
		IntBufferedArray.result(fastUtilArray, startTime, freemem);

		// Testing Buffered Array
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		FloatBufferedArray floatBufferedArray = new FloatBufferedArray(size * 4);
		for (float v : randomArray)
			floatBufferedArray.add(v);
		floatBufferedArray.getFinalArray();
		IntBufferedArray.result(floatBufferedArray, startTime, freemem);
	}
}
