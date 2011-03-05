/**   
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.File;

import com.jaeksoft.searchlib.SearchLibException;

public class LastModifiedAndSize implements RecursiveDirectoryBrowser.CallBack {

	private final boolean fileOnly;

	private long lastModified;

	private long size;

	private long count;

	private File lastModifiedFile;

	public LastModifiedAndSize(File file, boolean fileOnly)
			throws SearchLibException {
		this.fileOnly = fileOnly;
		lastModifiedFile = file;
		lastModified = 0;
		size = 0;
		count = 0;
		file(file);
		new RecursiveDirectoryBrowser(file, this);
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getSize() {
		return size;
	}

	public File getLastModifiedFile() {
		return lastModifiedFile;
	}

	public long getCount() {
		return count;
	}

	@Override
	public final void file(File file) throws SearchLibException {
		if (fileOnly && file.isDirectory())
			return;
		long l = file.lastModified();
		if (l > lastModified) {
			lastModified = l;
			lastModifiedFile = file;
		}
		size += file.length();
		count++;
	}

}
