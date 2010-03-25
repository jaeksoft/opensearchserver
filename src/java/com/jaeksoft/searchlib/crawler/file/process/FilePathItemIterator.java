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

import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;

public class FilePathItemIterator {

	private final Lock lock = new ReentrantLock(true);

	private ItemIterator itemIterator;

	protected FilePathItemIterator(FilePathItem filePathItem) {
		lock.lock();
		try {
			itemIterator = ItemIterator.create(null,
					filePathItem.getFilePath(), filePathItem.isWithSub());
		} finally {
			lock.unlock();
		}
	}

	protected File next() {
		lock.lock();
		try {
			for (;;) {
				if (itemIterator == null)
					return null;
				File f = itemIterator.getFile();
				itemIterator = itemIterator.next();
				if (f != null)
					return f;
			}
		} finally {
			lock.unlock();
		}
	}
}
