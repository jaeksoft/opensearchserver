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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;

public class CrawlFileIterator {

	private Iterator<FilePathItem> filePathItemListIterator;

	private FilePathItemIterator filePathItemIterator;

	private final Lock lock = new ReentrantLock(true);

	private boolean hasNext;

	protected CrawlFileIterator(FilePathManager filePathManager) {
		lock.lock();
		try {
			hasNext = true;
			List<FilePathItem> filePathItemList = new ArrayList<FilePathItem>();
			filePathManager.getAllFilePaths(filePathItemList);
			filePathItemListIterator = filePathItemList.iterator();
			filePathItemIterator = null;
		} finally {
			lock.unlock();
		}
	}

	protected FileInstanceAbstract next() throws URISyntaxException,
			InstantiationException, IllegalAccessException {
		lock.lock();
		try {
			for (;;) {
				if (filePathItemIterator == null) {
					if (!filePathItemListIterator.hasNext())
						return null;
					FilePathItem filePathItem = filePathItemListIterator.next();
					filePathItemIterator = new FilePathItemIterator(
							filePathItem);
				} else {
					FileInstanceAbstract next = filePathItemIterator.next();
					if (next != null)
						return next;
					hasNext = false;
					filePathItemIterator = null;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean hasNext() {
		lock.lock();
		try {
			return hasNext;
		} finally {
			lock.unlock();
		}
	}
}
