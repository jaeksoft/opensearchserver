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

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.LocalFileInstance;

public class StreamLimiterFileInstance extends StreamLimiter {

	private final FileInstanceAbstract fileInstance;

	public StreamLimiterFileInstance(FileInstanceAbstract fileInstance,
			int limit) throws IOException {
		super(limit);
		this.fileInstance = fileInstance;
	}

	@Override
	protected void loadOutputCache() throws LimitException, IOException {
		loadOutputCache(fileInstance.getInputStream());
	}

	@Override
	public File getFile() throws SearchLibException {
		try {
			if (fileInstance instanceof LocalFileInstance)
				return ((LocalFileInstance) fileInstance).getFile();
			String ext = FilenameUtils.getExtension(fileInstance.getFileName());
			return getTempFile(ext);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

}
