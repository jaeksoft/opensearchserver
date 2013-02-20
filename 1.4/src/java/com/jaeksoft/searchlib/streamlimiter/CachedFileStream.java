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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CachedFileStream implements CachedStreamInterface {

	private final File file;

	public CachedFileStream(File file, long limit) throws LimitException,
			IOException {
		this.file = file;
		if (limit != 0)
			if (getSize() > limit)
				throw new LimitException("File " + file.getName()
						+ " larger than " + limit + " bytes.");
	}

	@Override
	public InputStream getNewInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	public long getSize() {
		return file.length();
	}

}
