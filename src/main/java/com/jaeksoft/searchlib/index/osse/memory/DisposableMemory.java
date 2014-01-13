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

public class DisposableMemory extends Pointer implements Closeable {

	DisposableMemory(long size) {
		super(Native.malloc(size));
		if (peer == 0)
			throw new OutOfMemoryError("Cannot allocate " + size + " bytes");
	}

	@Override
	public void finalize() {
		close();
	}

	long getPeer() {
		return peer;
	}

	@Override
	public void close() {
		if (peer == 0)
			return;
		Native.free(peer);
		peer = 0;
	}
}
