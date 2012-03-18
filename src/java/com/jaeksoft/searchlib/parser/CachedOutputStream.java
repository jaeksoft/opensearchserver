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

package com.jaeksoft.searchlib.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.jaeksoft.searchlib.util.Md5Spliter;

public class CachedOutputStream extends ByteArrayOutputStream {

	private String hashMD5 = null;

	public CachedOutputStream(InputStream inputStream, int limit)
			throws LimitException, IOException {
		boolean bNoLimit = (limit == 0);
		byte[] buffer = new byte[65536];
		int bufferSize = 0;
		while ((bufferSize = inputStream.read(buffer)) != -1) {
			write(buffer, 0, bufferSize);
			if (!bNoLimit) {
				limit -= bufferSize;
				if (limit < 0)
					throw new LimitException();
			}
		}
	}

	public InputStream getNewInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	}

	public String getMD5Hash() throws NoSuchAlgorithmException {
		if (hashMD5 != null)
			return hashMD5;
		hashMD5 = Md5Spliter.getMD5Hash(buf, 0, count);
		return hashMD5;
	}

	public byte[] getBytes() {
		return buf;
	}

}
