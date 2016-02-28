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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.LocalFileInstance;
import com.jaeksoft.searchlib.util.IOUtils;

public class StreamLimiterFileInstance extends StreamLimiter {

	private final FileInstanceAbstract fileInstance;

	public StreamLimiterFileInstance(FileInstanceAbstract fileInstance, int limit)
			throws IOException, SearchLibException {
		super(limit, fileInstance.getFileName(), fileInstance.getURL());
		this.fileInstance = fileInstance;
	}

	@Override
	protected void loadOutputCache() throws LimitException, IOException {
		Long l = fileInstance.getFileSize();
		if (l != null)
			if (limit != 0 && l > limit)
				throw new LimitException(
						"File instance " + fileInstance.getFileName() + " larger than " + limit + " bytes.");
		if (fileInstance instanceof LocalFileInstance)
			loadOutputCache(((LocalFileInstance) fileInstance).getFile());
		else {
			InputStream is = null;
			BufferedInputStream bis = null;
			try {
				is = fileInstance.getInputStream();
				bis = new BufferedInputStream(is);
				loadOutputCache(bis);
			} finally {
				IOUtils.close(is, bis);
			}
		}
	}

	@Override
	public File getFile() throws IOException, SearchLibException {
		if (fileInstance instanceof LocalFileInstance)
			return ((LocalFileInstance) fileInstance).getFile();
		String ext = FilenameUtils.getExtension(fileInstance.getFileName());
		return getTempFile(ext);
	}

}
