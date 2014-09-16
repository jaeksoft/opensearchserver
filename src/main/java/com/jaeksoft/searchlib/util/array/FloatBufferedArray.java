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

public class FloatBufferedArray extends FloatArrayList {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6828586845790205300L;

	private float[] finalArray = null;

	public FloatBufferedArray(final int maxSize, final int arraySize) {
		super(arraySize);
	}

	public FloatBufferedArray(final int maxSize) {
		super(1024);
	}

	public final float[] getFinalArray() {
		if (finalArray == null)
			finalArray = toFloatArray();
		return finalArray;
	}

	@Override
	public final void clear() {
		super.clear();
		finalArray = null;
	}

}
