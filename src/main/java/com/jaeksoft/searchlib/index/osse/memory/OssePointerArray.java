/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

import java.io.Closeable;
import java.util.Collection;

import com.sun.jna.Pointer;

/**
 * This class implements a fast Pointer array *
 */
public class OssePointerArray extends Pointer implements Closeable {

	private final DisposableMemory memory;

	public OssePointerArray(final MemoryBuffer memoryBuffer,
			final Collection<Pointer> pointers) {
		super(0);
		memory = memoryBuffer.getNewBufferItem((pointers.size() + 1)
				* Pointer.SIZE);
		peer = memory.getPeer();
		int i = 0;
		for (Pointer pointer : pointers) {
			memory.setPointer(Pointer.SIZE * i, pointer);
			i++;
		}
		memory.setPointer(Pointer.SIZE * i, null);
	}

	public OssePointerArray(final MemoryBuffer memoryBuffer,
			final Pointer pointer) {
		super(0);
		memory = memoryBuffer.getNewBufferItem(2 * Pointer.SIZE);
		peer = memory.getPeer();
		memory.setPointer(0, pointer);
		memory.setPointer(Pointer.SIZE, null);
	}

	@Override
	public void close() {
		memory.close();
	}
}
