/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jaeksoft.searchlib.streamlimiter.CachedMemoryStream.MaxMemoryException;
import com.jaeksoft.searchlib.util.IOUtils;

public class CachedFileStream implements CachedStreamInterface {

	private final File file;
	private final boolean isTemp;

	public CachedFileStream(File file, long limit) throws LimitException,
			IOException {
		this.file = file;
		this.isTemp = false;
		if (limit != 0)
			if (getSize() > limit)
				throw new LimitException("File " + file.getName()
						+ " larger than " + limit + " bytes.");
	}

	CachedFileStream(MaxMemoryException mme, InputStream inputStream, long limit)
			throws IOException {
		FileOutputStream output = null;
		this.isTemp = true;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("CachedFileStream", "cache");
			output = new FileOutputStream(tempFile);
			output.write(mme.buf, 0, mme.count);
			int bufferSize = 0;
			byte[] buffer = new byte[65536];
			while ((bufferSize = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bufferSize);
				limit = CachedMemoryStream.checkLimit(limit, bufferSize);
			}
			file = tempFile;
		} catch (IOException e) {
			if (tempFile != null)
				tempFile.delete();
			throw e;
		} finally {
			IOUtils.closeQuietly(output);
		}
	}

	@Override
	public InputStream getNewInputStream() throws FileNotFoundException {
		if (file.isDirectory())
			return null;
		return new FileInputStream(file);
	}

	@Override
	public long getSize() {
		return file.length();
	}

	@Override
	public void close() throws IOException {
		if (isTemp && file != null)
			file.delete();
	}

}
