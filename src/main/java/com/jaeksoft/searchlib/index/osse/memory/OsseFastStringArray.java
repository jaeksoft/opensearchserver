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

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;

import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate.OsseTerm;
import com.jaeksoft.searchlib.index.osse.OsseTokenTermUpdate.OsseTermBuffer;
import com.jaeksoft.searchlib.util.StringUtils;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * This class implements a fast UTF-8 String array *
 */
public class OsseFastStringArray extends DisposableMemory {

	/**
	 * Optimized write only StringArray
	 * 
	 * @param strings
	 */

	private final DisposableMemory fullBytes;

	public OsseFastStringArray(final OsseTermBuffer termBuffer)
			throws UnsupportedEncodingException, CharacterCodingException {
		super((termBuffer.length + 1) * Pointer.SIZE);
		fullBytes = mallocOfTermBuffer(termBuffer);
	}

	private final DisposableMemory mallocOfTermBuffer(OsseTermBuffer termBuffer) {
		final DisposableMemory memory = new DisposableMemory(
				termBuffer.bytesSize + termBuffer.length);
		final long peer = Memory.nativeValue(memory);
		long offset = 0;
		int i = 0;
		int l = termBuffer.length;
		for (OsseTerm term : termBuffer.terms) {
			if (i == l)
				break;
			setPointer(Pointer.SIZE * i, new Pointer(peer + offset));
			memory.write(offset, term.bytes, 0, term.bytes.length);
			offset += term.bytes.length;
			memory.setByte(offset, (byte) 0);
			offset++;
			i++;
		}
		setPointer(Pointer.SIZE * i, null);
		return memory;
	}

	@Override
	final public void close() {
		fullBytes.close();
		super.close();
	}

	@Override
	public String toString() {
		return StringUtils.fastConcat("[", super.toString(), " ",
				fullBytes.toString(), "]");
	}
}
