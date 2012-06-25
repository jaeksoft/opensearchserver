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

package com.jaeksoft.searchlib.crawler.file.process;

import java.net.URISyntaxException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.util.SimpleLock;

public class FilePathItemIterator {

	private final SimpleLock lock = new SimpleLock();

	private ItemIterator itemIterator;

	protected FilePathItemIterator(FilePathItem filePathItem)
			throws SearchLibException {
		lock.rl.lock();
		try {
			FileInstanceAbstract fileInstance = FileInstanceAbstract.create(
					filePathItem, null, filePathItem.getPath());
			itemIterator = ItemIterator.create(null, fileInstance,
					filePathItem.isWithSubDir());
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	protected ItemIterator next() throws URISyntaxException, SearchLibException {
		lock.rl.lock();
		try {
			for (;;) {
				if (itemIterator == null)
					return null;
				ItemIterator i = itemIterator;
				itemIterator = itemIterator.next();
				return i;
			}
		} finally {
			lock.rl.unlock();
		}
	}
}
