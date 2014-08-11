/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.imageio.stream.ImageInputStream;

import com.jaeksoft.searchlib.Logging;

public class IOUtils extends org.apache.commons.io.IOUtils {

	public static final void close(final Closeable closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		} catch (IOException e) {
			Logging.warn(e);
		}
	}

	public static final void close(final Closeable... closeables) {
		if (closeables == null)
			return;
		for (Closeable closeable : closeables)
			closeQuietly(closeable);
	}

	public static final void close(ImageInputStream... closeables) {
		if (closeables == null)
			return;
		for (ImageInputStream closeable : closeables)
			closeQuietly(closeable);
	}

	public static final void closeQuietly(ImageInputStream closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		} catch (IOException e) {
			// We said Quietly
		}
	}

	public static final void close(
			final Collection<? extends Closeable> closeables) {
		if (closeables == null)
			return;
		for (Closeable closeable : closeables)
			closeQuietly(closeable);
	}

	public static final int copy(InputStream inputStream, File tempFile,
			boolean bCloseInputStream) throws IOException {
		FileOutputStream fos = new FileOutputStream(tempFile);
		try {
			return copy(inputStream, fos);
		} finally {
			close(fos);
			if (bCloseInputStream)
				close(inputStream);
		}
	}

	public static final StringBuilder copy(InputStream inputStream,
			StringBuilder sb, String charsetName, boolean bCloseInputStream)
			throws IOException {
		if (inputStream == null)
			return sb;
		if (sb == null)
			sb = new StringBuilder();
		Charset charset = Charset.forName(charsetName);
		byte[] buffer = new byte[16384];
		int length;
		while ((length = inputStream.read(buffer)) != -1)
			sb.append(new String(buffer, 0, length, charset));
		if (bCloseInputStream)
			inputStream.close();
		return sb;
	}

}
