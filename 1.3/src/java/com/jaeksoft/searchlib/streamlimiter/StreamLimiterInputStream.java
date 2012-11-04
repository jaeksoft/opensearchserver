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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.SearchLibException;

public class StreamLimiterInputStream extends StreamLimiter {

	private final InputStream inputStream;

	public StreamLimiterInputStream(long limit, InputStream inputStream,
			String originalFileName) throws IOException {
		super(limit, originalFileName);
		this.inputStream = inputStream;
	}

	@Override
	protected void loadOutputCache() throws LimitException, IOException {
		loadOutputCache(inputStream);
	}

	@Override
	public File getFile() throws SearchLibException {
		try {
			String ext = originalFileName == null ? null : FilenameUtils
					.getExtension(originalFileName);
			return getTempFile(ext);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

}
