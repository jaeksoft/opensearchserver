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
package com.jaeksoft.searchlib.util.array;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

import java.util.Random;

import com.jaeksoft.searchlib.util.NativeOss;

public abstract class FloatBufferedArrayFactory {

	public final static FloatBufferedArrayFactory INSTANCE = NativeOss.loaded() ? new NativeFactory()
			: new JavaFactory();

	public abstract FloatBufferedArrayInterface newInstance(final long maxSize);

	final static private class NativeFactory extends FloatBufferedArrayFactory {

		@Override
		public FloatBufferedArrayInterface newInstance(final long maxSize) {
			return new NativeFloatBufferedArray(maxSize);
		}
	}

	final static private class JavaFactory extends FloatBufferedArrayFactory {

		@Override
		public FloatBufferedArrayInterface newInstance(final long maxSize) {
			return new FloatBufferedArray(maxSize);
		}
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
		IntBufferedArrayFactory.result(randomArray, startTime, freemem);

		// Testing Native Array reduced size
		System.gc();
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		float[] nativeArray1 = new float[size];
		int i = 0;
		for (float v : randomArray)
			nativeArray1[i++] = v;
		IntBufferedArrayFactory.result(nativeArray1, startTime, freemem);
		check(randomArray, nativeArray1);
		IntBufferedArrayFactory.result(nativeArray1, startTime, freemem);

		// Testing Native Array
		System.gc();
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		float[] nativeArray = new float[size * 4];
		i = 0;
		for (float v : randomArray)
			nativeArray[i++] = v;
		IntBufferedArrayFactory.result(nativeArray, startTime, freemem);
		check(randomArray, nativeArray);
		IntBufferedArrayFactory.result(nativeArray, startTime, freemem);

		// Testing FastUTIL
		System.gc();
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		FloatArrayList fastUtilArray = new FloatArrayList(size * 4);
		for (float v : randomArray)
			fastUtilArray.add(v);
		IntBufferedArrayFactory.result(fastUtilArray, startTime, freemem);
		check(randomArray, fastUtilArray.toFloatArray());
		IntBufferedArrayFactory.result(fastUtilArray, startTime, freemem);

		// Testing Buffered Array
		System.gc();
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		FloatBufferedArray floatBufferedArray = new FloatBufferedArray(size * 4);
		for (float v : randomArray)
			floatBufferedArray.add(v);
		IntBufferedArrayFactory.result(floatBufferedArray, startTime, freemem);
		check(randomArray, floatBufferedArray.getFinalArray());
		IntBufferedArrayFactory.result(floatBufferedArray, startTime, freemem);

		// Testing Native Array
		System.gc();
		startTime = System.currentTimeMillis();
		freemem = Runtime.getRuntime().freeMemory();
		FloatBufferedArrayInterface ibai = INSTANCE.newInstance(size * 4);
		for (float v : randomArray)
			ibai.add(v);
		IntBufferedArrayFactory.result(ibai, startTime, freemem);
		check(randomArray, ibai.getFinalArray());
		IntBufferedArrayFactory.result(ibai, startTime, freemem);
	}

	private static void check(float[] randomArray, float[] finalArray) {
		if (randomArray.length > finalArray.length) {
			System.err.println("BufferedArray corrupted (size)");
			return;
		}
		int pos = 0;
		for (float value : randomArray)
			if (finalArray[pos++] != value) {
				System.err.println("BufferedArray corrupted (content) " + pos);
				return;
			}
		System.out.println("BufferedArray ok");
	}
}
