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

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class StreamLimiterBase64 extends StreamLimiter {

	private final String base64text;
	private final String extension;

	public StreamLimiterBase64(String base64text, int limit, String fileName)
			throws IOException {
		super(limit);
		this.base64text = base64text;
		this.extension = fileName != null ? FilenameUtils
				.getExtension(fileName) : null;
	}

	@Override
	protected void loadOutputCache() throws LimitException, IOException {
		InputStream is = new LargeStringInputString(base64text, 131072);
		Base64InputStream b64is = new Base64InputStream(is);
		try {
			loadOutputCache(b64is);
		} finally {
			if (b64is != null)
				IOUtils.closeQuietly(b64is);
			if (is != null)
				IOUtils.closeQuietly(is);
		}

	}

	@Override
	public File getFile() throws IOException {
		return getTempFile(extension);
	}

}
