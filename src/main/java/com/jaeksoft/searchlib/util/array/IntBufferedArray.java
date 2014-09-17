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

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntBufferedArray {

	private final int maxSize;

	private int initialArraySize;

	private int nextArraySize;

	private final List<int[]> arrays;

	private int[] currentArray;

	private int currentArrayPos;

	private int[] finalArray;

	private int totalSize;

	public IntBufferedArray(final int maxSize, final int initialArraySize) {
		this.maxSize = maxSize;
		this.initialArraySize = initialArraySize;
		this.nextArraySize = initialArraySize;
		this.arrays = new ArrayList<int[]>();
		this.totalSize = 0;
		newCurrentArray();
	}

	public IntBufferedArray(final int maxSize) {
		this(maxSize, 16384);
	}

	final void newCurrentArray() {
		currentArray = new int[nextArraySize];
		arrays.add(currentArray);
		currentArrayPos = 0;
		if (nextArraySize > maxSize - totalSize)
			nextArraySize = maxSize - totalSize;
	}

	final public void add(final int value) {
		if (currentArrayPos == currentArray.length)
			newCurrentArray();
		currentArray[currentArrayPos++] = value;
		totalSize++;
	}

	final public int getSize() {
		return totalSize;
	}

	final public int[] getFinalArray() {
		if (finalArray != null)
			return finalArray;
		finalArray = new int[totalSize];
		int sizeLeft = totalSize;
		int buffer;
		int pos = 0;
		for (int[] array : arrays) {
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

	public static final void result(Object object, long startTime, long freemem) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println(object.getClass().getSimpleName() + " Time: "
				+ elapsedTime + " Memory: "
				+ (freemem - Runtime.getRuntime().freeMemory()) / 1024);
	}

	public final static void main(String[] str) {
		final int size = 10000000;

		Random random = new Random(System.currentTimeMillis());
		// Building the index
		long startTime = System.currentTimeMillis();
		long freemem = Runtime.getRuntime().freeMemory();
		int[] randomArray = new int[size];
		for (int i = 0; i < size; i++)
			randomArray[i++] = random.nextInt();
		result(randomArray, startTime, freemem);

		// Testing Native Array
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		int[] nativeArray1 = new int[size];
		int i = 0;
		for (int v : randomArray)
			nativeArray1[i++] = v;
		result(nativeArray1, startTime, freemem);

		// Testing Native Array
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		int[] nativeArray = new int[size * 4];
		i = 0;
		for (int v : randomArray)
			nativeArray[i++] = v;
		result(nativeArray, startTime, freemem);

		// Testing FastUTIL
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		IntArrayList fastUtilArray = new IntArrayList(size * 4);
		for (int v : randomArray)
			fastUtilArray.add(v);
		fastUtilArray.toIntArray();
		result(fastUtilArray, startTime, freemem);

		// Testing Buffered Array
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		IntBufferedArray intBufferedArray = new IntBufferedArray(size * 4);
		for (int v : randomArray)
			intBufferedArray.add(v);
		intBufferedArray.getFinalArray();
		result(intBufferedArray, startTime, freemem);
	}
}
