/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedMemoryStream extends ByteArrayOutputStream implements
		CachedStreamInterface {

	public class MaxMemoryException extends Exception {

		private static final long serialVersionUID = -2377324978314552922L;

		public final byte[] buf;
		public final int count;

		private MaxMemoryException(byte[] buf, int count) {
			this.buf = buf;
			this.count = count;
		}
	}

	private CachedMemoryStream(InputStream inputStream, long limit,
			long maxMemoryCache) throws LimitException, IOException,
			MaxMemoryException {
		byte[] buffer = new byte[65536];
		int bufferSize = 0;
		while ((bufferSize = inputStream.read(buffer)) != -1) {
			write(buffer, 0, bufferSize);
			limit = checkLimit(limit, bufferSize);
			if (maxMemoryCache != 0) {
				maxMemoryCache -= bufferSize;
				if (maxMemoryCache <= 0)
					throw new MaxMemoryException(buf, count);
			}
		}
	}

	public static final long checkLimit(long limit, int bufferSize)
			throws LimitException {
		if (limit == 0)
			return limit;
		limit -= bufferSize;
		if (limit < 0)
			throw new LimitException("Stream larger than " + limit + " bytes.");
		return limit;
	}

	private final static int MAX_MEMORY_CACHE = 1024 * 1024 * 10;

	public static CachedStreamInterface getCachedStream(
			InputStream inputStream, long limit) throws LimitException,
			IOException {
		try {
			return new CachedMemoryStream(inputStream, limit, MAX_MEMORY_CACHE);
		} catch (MaxMemoryException e) {
			return new CachedFileStream(e, inputStream, limit);
		}
	}

	@Override
	public InputStream getNewInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	}

	@Override
	public long getSize() {
		return count;
	}

}
