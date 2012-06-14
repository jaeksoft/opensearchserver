/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

public class StreamLimiter {

	final private CachedOutputStream outputCache;

	public StreamLimiter(InputStream inputStream, int limit) throws IOException {
		outputCache = new CachedOutputStream(inputStream, limit);
	}

	public InputStream getNewInputStream() throws IOException {
		return outputCache.getNewInputStream();
	}

	public String getMD5Hash() throws NoSuchAlgorithmException {
		return outputCache.getMD5Hash();
	}

	public int size() {
		return outputCache.size();
	}

	public byte[] getBytes() {
		return outputCache.getBytes();
	}
}
