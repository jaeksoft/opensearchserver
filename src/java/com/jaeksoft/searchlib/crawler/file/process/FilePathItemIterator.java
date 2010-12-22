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

import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;

public class FilePathItemIterator {

	private final Lock lock = new ReentrantLock(true);

	private ItemIterator itemIterator;

	protected FilePathItemIterator(FilePathItem filePathItem)
			throws URISyntaxException, InstantiationException,
			IllegalAccessException {
		lock.lock();
		try {
			FileInstanceAbstract fileInstance = FileInstanceAbstract.create(
					filePathItem, null, filePathItem.getPath());
			itemIterator = ItemIterator.create(null, fileInstance,
					filePathItem.isWithSub());
		} finally {
			lock.unlock();
		}
	}

	protected FileInstanceAbstract next() throws URISyntaxException {
		lock.lock();
		try {
			for (;;) {
				if (itemIterator == null)
					return null;
				FileInstanceAbstract f = itemIterator.getFileInstance();
				itemIterator = itemIterator.next();
				if (f != null)
					return f;
			}
		} finally {
			lock.unlock();
		}
	}
}
