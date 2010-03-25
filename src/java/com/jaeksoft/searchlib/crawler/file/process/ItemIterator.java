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

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ItemIterator {

	private final Lock lock = new ReentrantLock(true);

	protected ItemIterator parent;

	protected ItemIterator(ItemIterator parent) {
		lock.lock();
		try {
			this.parent = parent;
		} finally {
			lock.unlock();
		}
	}

	protected File getFile() {
		lock.lock();
		try {
			return getFileImpl();
		} finally {
			lock.unlock();
		}
	}

	protected abstract File getFileImpl();

	protected ItemIterator next() {
		lock.lock();
		try {
			return nextImpl();
		} finally {
			lock.unlock();
		}
	}

	protected abstract ItemIterator nextImpl();

	protected static ItemIterator create(ItemIterator parent, File file,
			boolean withSubDir) {
		if (file.isDirectory())
			return new ItemDirectoryIterator(parent, file, withSubDir);
		if (file.isFile())
			return new ItemFileIterator(parent, file);
		return null;
	}
}
