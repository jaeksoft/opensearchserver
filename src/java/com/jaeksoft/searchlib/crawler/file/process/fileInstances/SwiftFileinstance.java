/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class SwiftFileinstance extends FileInstanceAbstract {

	public SwiftFileinstance() {
		super();
	}

	protected SwiftFileinstance(FilePathItem filePathItem,
			SwiftFileinstance parent, String path) throws URISyntaxException,
			SearchLibException {
		init(filePathItem, parent, path);
	}

	@Override
	public URI init() throws SearchLibException, URISyntaxException {
		return new URI("dropbox", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");
		// return FileTypeEnum.directory;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		// TODO
		throw new SearchLibException("To do");
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public Long getLastModified() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public Long getFileSize() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public String getFileName() throws SearchLibException {
		// TODO
		throw new SearchLibException("To do");

	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO
		throw new IOException("To do");

	}

}
