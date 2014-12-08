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

public class NativeFloatBufferedArray implements FloatBufferedArrayInterface {

	private long ref;

	private int pos;

	private float[] buffer = new float[16384];

	final private native long init(final long maxSize);

	NativeFloatBufferedArray(long maxSize) {
		ref = init(maxSize);
	}

	final private native void free(final long ref);

	@Override
	protected void finalize() {
		free(ref);
		ref = 0;
	}

	final private void flushBuffer() {
		add(ref, buffer, pos);
		pos = 0;
	}

	final private native void add(final long ref, final float[] buffer,
			final int length);

	@Override
	final public void add(final float value) {
		if (pos == buffer.length)
			flushBuffer();
		buffer[pos++] = value;
	}

	final private native long getSize(final long ref);

	@Override
	final public long getSize() {
		return getSize(ref) + pos;
	}

	public native void populateFinalArray(final long ref,
			final float[] finalArray);

	@Override
	final public float[] getFinalArray() {
		if (pos > 0)
			flushBuffer();
		float[] finalArray = new float[(int) getSize()];
		populateFinalArray(ref, finalArray);
		return finalArray;
	}

}
