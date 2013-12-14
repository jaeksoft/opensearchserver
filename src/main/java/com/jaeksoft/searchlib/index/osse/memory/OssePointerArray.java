/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse.memory;

import com.sun.jna.Pointer;

/**
 * This class implements a fast UTF-8 String array *
 */
public class OssePointerArray extends DisposableMemory {

	/**
	 * Optimized Pointer array
	 * 
	 * @param strings
	 */

	public OssePointerArray(Pointer... pointers) {
		super((pointers.length + 1) * Pointer.SIZE);
		int i = 0;
		for (Pointer pointer : pointers) {
			setPointer(Pointer.SIZE * i, pointer);
			i++;
		}
		setPointer(Pointer.SIZE * i, null);
	}

}
