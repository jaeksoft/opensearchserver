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

import java.util.Collection;

import com.sun.jna.Pointer;

/**
 * This class implements a fast UTF-8 String array *
 */
public class OssePointerArray extends DisposableMemory {

	public static interface PointerProvider {
		Pointer getPointer();
	}

	public OssePointerArray(Collection<? extends PointerProvider> pointers) {
		super((pointers.size() + 1) * Pointer.SIZE);
		int i = 0;
		for (PointerProvider pointerProvider : pointers) {
			setPointer(Pointer.SIZE * i, pointerProvider.getPointer());
			i++;
		}
		setPointer(Pointer.SIZE * i, null);
	}

	public OssePointerArray(Pointer pointer) {
		super(2 * Pointer.SIZE);
		setPointer(0, pointer);
		setPointer(Pointer.SIZE, null);
	}
}
