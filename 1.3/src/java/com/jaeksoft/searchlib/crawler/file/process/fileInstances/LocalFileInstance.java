/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class LocalFileInstance extends FileInstanceAbstract {

	private class FileOnlyFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			return f.isFile();
		}

	}

	private File file;

	public LocalFileInstance() {
		file = null;
	}

	private LocalFileInstance(FilePathItem filePathItem,
			FileInstanceAbstract parent, File file) throws URISyntaxException,
			SearchLibException {
		init(filePathItem, parent, file.getAbsolutePath());
		this.file = file;
	}

	@Override
	public URI init() {
		file = new File(getPath());
		return file.toURI();
	}

	@Override
	public FileTypeEnum getFileType() {
		if (file.isFile())
			return FileTypeEnum.file;
		if (file.isDirectory())
			return FileTypeEnum.directory;
		return null;
	}

	public File getFile() {
		return file;
	}

	private FileInstanceAbstract[] buildFileInstanceArray(File[] files)
			throws URISyntaxException, SearchLibException {
		if (files == null)
			return null;
		FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[files.length];
		int i = 0;
		for (File f : files)
			fileInstances[i++] = new LocalFileInstance(filePathItem, this, f);
		return fileInstances;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		return buildFileInstanceArray(file.listFiles());
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException {
		return buildFileInstanceArray(file.listFiles(new FileOnlyFilter()));
	}

	@Override
	public Long getLastModified() {
		return file.lastModified();
	}

	@Override
	public Long getFileSize() {
		return file.length();
	}

	@Override
	public FileInputStream getInputStream() throws IOException {
		if (file == null)
			return null;
		return new FileInputStream(file);
	}

	@Override
	public String getFileName() throws SearchLibException {
		return file.getName();
	}

}
