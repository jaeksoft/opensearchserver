/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitInputStream extends InputStream {

	private boolean isComplete;
	private InputStream inputStream;
	private long limit;
	private ByteArrayOutputStream outputCache;
	private ByteArrayInputStream inputCache;

	public LimitInputStream(InputStream inputStream, long limit) {
		this.inputStream = inputStream;
		this.limit = limit;
		this.isComplete = false;
		outputCache = new ByteArrayOutputStream();
		inputCache = null;
	}

	@Override
	public int read() throws IOException {
		if (inputCache != null)
			return inputCache.read();
		if (limit-- == 0)
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

	public void restartFromCache() {
		inputCache = new ByteArrayInputStream(outputCache.toByteArray());
	}
}
