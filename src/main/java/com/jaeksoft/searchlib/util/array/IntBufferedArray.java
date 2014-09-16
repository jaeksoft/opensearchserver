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

public class IntBufferedArray extends AbstractBufferedArray {

	private List<Object> arrays;

	private int[] currentArray;

	public IntBufferedArray(final int maxSize, final int arraySize) {
		super(maxSize, arraySize);
	}

	public IntBufferedArray(final int maxSize) {
		super(maxSize, 1024);
	}

	@Override
	final protected void buildArrays() {
		arrays = new ArrayList<Object>();
	}

	@Override
	final protected void newCurrentArray(final int arraySize) {
		currentArray = new int[arraySize];
		arrays.add(currentArray);
	}

	final public void add(final int value) {
		int currentArrayPos = checkBeforeAdd();
		currentArray[currentArrayPos] = value;
	}

	@Override
	public final int[] getFinalArray() {
		return (int[]) super.getFinalArray();
	}

	@Override
	protected Object newFinalArray(int size) {
		return new int[size];
	}

	@Override
	protected List<Object> getArrays() {
		return arrays;
	}

	@Override
	final protected void clear() {
		arrays = null;
		currentArray = null;
	}
}
