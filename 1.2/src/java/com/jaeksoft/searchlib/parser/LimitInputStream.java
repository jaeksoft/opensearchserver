/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

public class LimitInputStream extends InputStream {

	private boolean isComplete;
	private InputStream inputStream;
	private long limit;
	final private ByteArrayOutputStream outputCache;
	private ByteArrayInputStream inputCache;
	private String hashMD5;
	final private boolean bNoLimit;

	public LimitInputStream(InputStream inputStream, long limit)
			throws IOException {
		this.inputStream = inputStream;
		this.limit = limit;
		this.bNoLimit = (limit == 0);
		this.isComplete = false;
		outputCache = new ByteArrayOutputStream();
		inputCache = null;
		hashMD5 = null;
	}

	@Override
	final public int read() throws IOException {
		if (inputCache != null)
			return inputCache.read();
		if (!bNoLimit && limit-- == 0)
			throw new LimitException();
		int i = inputStream.read();
		if (i == -1) {
			isComplete = true;
			return -1;
		}
		outputCache.write(i);
		return i;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public String getMD5Hash() throws NoSuchAlgorithmException {
		if (!isComplete)
			return null;
		if (hashMD5 != null)
			return hashMD5;
		hashMD5 = Md5Spliter.getMD5Hash(outputCache.toString().getBytes());
		return hashMD5;
	}

	public Integer getSize() {
		if (!isComplete)
			return null;
		return outputCache.size();
	}

	public void consume() throws IOException {
		while (!isComplete)
			while (read() != -1)
				;
	}

	public void restartFromCache() {
		if (inputCache == null)
			inputCache = new ByteArrayInputStream(outputCache.toByteArray());
		else
			inputCache.reset();
	}

}
