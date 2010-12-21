/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process;

import java.net.URI;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.LocalFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SmbFileInstance;

public abstract class FileInstanceAbstract {

	protected URI uri;

	protected FileInstanceAbstract parent;

	final public static FileInstanceAbstract create(
			FileInstanceAbstract parent, URI uri) {
		if (uri == null)
			return null;
		String scheme = uri.getScheme();
		if ("smb".equals(scheme))
			return new SmbFileInstance(parent, uri);
		if ("ftp".equals(scheme))
			return new FtpFileInstance(parent, uri);
		if ("file".equals(scheme))
			return new LocalFileInstance(parent, uri);
		return null;
	}

	protected FileInstanceAbstract(FileInstanceAbstract parent, URI uri) {
		this.parent = parent;
		this.uri = uri;
	}

	public abstract FileTypeEnum getFileType();

	public abstract FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException;

	public abstract FileInstanceAbstract[] listFilesOnly()
			throws URISyntaxException;

	public abstract Long getLastModified();

	public abstract Long getFileSize();

	public URI getURI() {
		return uri;
	}

	public FileInstanceAbstract getParent() {
		return parent;
	}

}
