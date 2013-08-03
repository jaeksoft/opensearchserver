/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.streamlimiter;

import java.io.IOException;
import java.io.InputStream;

public class LargeStringInputString extends InputStream {

	final private String source;
	final private int bufferSize;
	private int start;
	private int bufferPos;
	final private int sourceLength;
	private int sizeLeft;
	private char[] buffer;

	public LargeStringInputString(String source, int bufferSize) {
		this.source = source;
		this.bufferSize = bufferSize;
		start = 0;
		bufferPos = 0;
		sourceLength = source.length();
		sizeLeft = sourceLength;
		buffer = new char[0];
	}

	final private boolean nextBuffer() {
		int l = bufferSize < sizeLeft ? bufferSize : sizeLeft;
		if (l == 0)
			return false;
		buffer = new char[l];
		source.getChars(start, start + l, buffer, 0);
		bufferPos = 0;
		start += l;
		sizeLeft -= l;
		return true;
	}

	@Override
	public int read() throws IOException {
		if (bufferPos == buffer.length)
			if (!nextBuffer())
				return -1;
		return buffer[bufferPos++];
	}
}
