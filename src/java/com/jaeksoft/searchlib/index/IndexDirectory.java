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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class IndexDirectory {

	private Directory directory;
	private final ReadWriteLock rwl = new ReadWriteLock();

	protected IndexDirectory(File indexDir) throws IOException {
		directory = FSDirectory.open(indexDir);
	}

	public Directory getDirectory() {
		rwl.r.lock();
		try {
			return directory;
		} finally {
			rwl.r.unlock();
		}
	}

	public void unlock() {
		rwl.w.lock();
		try {
			if (directory == null)
				return;
			if (!IndexWriter.isLocked(directory))
				return;
			IndexWriter.unlock(directory);
		} catch (IOException e) {
			Logging.warn(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void close() {
		rwl.w.lock();
		try {
			if (directory == null)
				return;
			try {
				directory.close();
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
			}
			directory = null;
		} finally {
			rwl.w.unlock();
		}
	}

}
