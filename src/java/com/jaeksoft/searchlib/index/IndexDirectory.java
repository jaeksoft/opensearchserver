/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.jaeksoft.searchlib.Logging;

public class IndexDirectory {

	private Directory directory;
	private File indexDir;
	private String name;

	protected IndexDirectory(String name, File indexDir) throws IOException {
		this.indexDir = indexDir;
		this.name = name;
		directory = FSDirectory.open(indexDir);
	}

	public Directory getDirectory() {
		synchronized (this) {
			return directory;
		}
	}

	public String getName() {
		synchronized (this) {
			return name;
		}
	}

	public void close() {
		synchronized (this) {
			if (directory == null)
				return;
			try {
				directory.close();
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
			}
			directory = null;
		}
	}

	static void deleteDir(File dir) {
		if (dir == null)
			return;
		File files[] = dir.listFiles();
		if (files != null)
			for (File f : files)
				f.delete();
		dir.delete();
	}

	public void delete() {
		synchronized (this) {
			close();
			deleteDir(this.indexDir);
		}
	}

}
