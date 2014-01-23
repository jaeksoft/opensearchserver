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

import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

final public class DisposableMemory extends Pointer implements
		BufferItemInterface {

	final int size;
	protected final MemoryBuffer buffer;
	private ByteBuffer byteBuffer;

	DisposableMemory(final MemoryBuffer buffer, final int size) {
		super(Native.malloc(size));
		if (peer == 0)
			throw new OutOfMemoryError("Cannot allocate " + size + " bytes");
		this.size = size;
		this.buffer = buffer;
		this.byteBuffer = null;
	}

	final public ByteBuffer getByteBuffer() {
		if (byteBuffer != null)
			return byteBuffer;
		byteBuffer = getByteBuffer(0, size);
		return byteBuffer;
	}

	@Override
	final public void finalize() {
		if (peer != 0)
			Native.free(peer);
		peer = 0;
	}

	final long getPeer() {
		return peer;
	}

	@Override
	final public void close() {
		if (buffer != null)
			buffer.closed(this);
	}

	@Override
	final public int getSize() {
		return size;
	}

	@Override
	final public DisposableMemory reset() {
		if (byteBuffer != null)
			byteBuffer.clear();
		return this;
	}
}
