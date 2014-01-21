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

import com.sun.jna.Native;
import com.sun.jna.Pointer;

final public class DisposableMemory extends Pointer implements Closeable {

	final long size;
	protected final MemoryBuffer buffer;

	DisposableMemory(final MemoryBuffer buffer, final long size) {
		super(Native.malloc(size));
		if (peer == 0)
			throw new OutOfMemoryError("Cannot allocate " + size + " bytes");
		this.size = size;
		this.buffer = buffer;
	}

	@Override
	final public void finalize() {
		Native.free(peer);
		peer = 0;
	}

	final long getPeer() {
		return peer;
	}

	@Override
	public void close() {
		if (peer == 0)
			return;
		if (buffer != null)
			buffer.closed(this);
	}
}
