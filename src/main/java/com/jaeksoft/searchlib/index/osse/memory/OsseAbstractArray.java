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

package com.jaeksoft.searchlib.index.osse.memory;

import java.io.Closeable;

import com.opensearchserver.utils.StringUtils;
import com.sun.jna.Pointer;

public abstract class OsseAbstractArray extends Pointer implements Closeable {

	protected final DisposableMemory arrayPointer;

	public OsseAbstractArray(final MemoryBuffer memoryBuffer,
			final int collectionSize, final int itemSize) {
		super(0);
		arrayPointer = memoryBuffer.getNewBufferItem(collectionSize * itemSize);
		peer = arrayPointer.getPeer();
	}

	@Override
	final public void close() {
		arrayPointer.close();
	}

	@Override
	final public String toString() {
		return StringUtils.fastConcat("[", super.toString(), " ", "]");
	}
}
