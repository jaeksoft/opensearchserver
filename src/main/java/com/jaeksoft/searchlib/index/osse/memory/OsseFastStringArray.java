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

import java.io.UnsupportedEncodingException;

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

	public OsseFastStringArray(final String[] strings, final int length)
			throws UnsupportedEncodingException {
		super((length + 1) * Pointer.SIZE);
		byte[][] bytesArray = new byte[length][];
		long totalSize = populateBytesCollection(strings, length, bytesArray);
		fullBytes = mallocOfBytesCollection(totalSize, bytesArray);
	}

	/**
	 * Retrieve all the bytes and get the total size
	 * 
	 * @param strings
	 * @param bytesCollection
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static final long populateBytesCollection(final String[] strings,
			final int length, final byte[][] bytesCollection)
			throws UnsupportedEncodingException {
		long size = 0;
		int i = 0;
		int pos = 0;
		for (String string : strings) {
			if (pos == length)
				break;
			pos++;
			if (string == null)
				continue;
			final byte[] bytes = string.getBytes("UTF-8");
			// DEBUG
			// System.out.println(i + " " + string + " (" + string.length() +
			// "/"
			// + bytes.length + ")");
			bytesCollection[i++] = bytes;
			size += bytes.length + 1;
		}
		return size;
	}

	private final DisposableMemory mallocOfBytesCollection(long totalSize,
			final byte[][] bytesCollection) {
		final DisposableMemory memory = new DisposableMemory(totalSize);
		final long peer = Memory.nativeValue(memory);
		long offset = 0;
		int i = 0;
		for (byte[] bytes : bytesCollection) {
			if (totalSize == 0)
				break;
			setPointer(Pointer.SIZE * i, new Pointer(peer + offset));
			memory.write(offset, bytes, 0, bytes.length);
			offset += bytes.length;
			memory.setByte(offset, (byte) 0);
			offset++;
			i++;
			totalSize -= bytes.length;
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
