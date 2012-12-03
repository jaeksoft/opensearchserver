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

package com.jaeksoft.searchlib.streamlimiter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.StringUtils;

public abstract class StreamLimiter implements Closeable {

	private final List<InputStream> inputStreamList;
	protected final long limit;
	private CachedStreamInterface outputCache;
	private final List<File> tempFiles;
	protected final String originalFileName;
	private String detectedCharset;

	protected StreamLimiter(long limit, String originalFileName)
			throws IOException {
		this.limit = limit;
		this.outputCache = null;
		this.inputStreamList = new ArrayList<InputStream>(0);
		this.tempFiles = new ArrayList<File>(0);
		this.originalFileName = originalFileName;
		this.detectedCharset = null;
	}

	public abstract File getFile() throws SearchLibException, IOException;

	final protected void loadOutputCache(InputStream inputStream)
			throws LimitException, IOException {
		if (outputCache != null)
			return;
		outputCache = new CachedMemoryStream(inputStream, limit);
	}

	final protected void loadOutputCache(File file) throws LimitException,
			IOException {
		if (outputCache != null)
			return;
		outputCache = new CachedFileStream(file, limit);
	}

	protected abstract void loadOutputCache() throws LimitException,
			IOException;

	public InputStream getNewInputStream() throws IOException {
		if (outputCache == null)
			loadOutputCache();
		InputStream inputStream = outputCache.getNewInputStream();
		inputStreamList.add(inputStream);
		return inputStream;
	}

	public String getMD5Hash() throws NoSuchAlgorithmException, LimitException,
			IOException {
		if (outputCache == null)
			loadOutputCache();
		InputStream is = null;
		try {
			is = getNewInputStream();
			return DigestUtils.md5Hex(is);
		} finally {
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

	public long getSize() throws LimitException, IOException {
		if (outputCache == null)
			loadOutputCache();
		return outputCache.getSize();
	}

	@Override
	public void close() throws IOException {
		for (InputStream inputStream : inputStreamList)
			IOUtils.closeQuietly(inputStream);
		inputStreamList.clear();
		for (File tmpFile : tempFiles)
			tmpFile.delete();
		tempFiles.clear();
	}

	protected File getTempFile(String extension) throws IOException {
		File tempFile = File.createTempFile("oss", "." + extension);
		FileUtils.copyInputStreamToFile(getNewInputStream(), tempFile);
		tempFiles.add(tempFile);
		return tempFile;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getDetectedCharset() throws IOException {
		if (detectedCharset != null)
			return detectedCharset;
		InputStream is = getNewInputStream();
		try {
			detectedCharset = StringUtils.charsetDetector(is);
			return detectedCharset;
		} finally {
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

}
