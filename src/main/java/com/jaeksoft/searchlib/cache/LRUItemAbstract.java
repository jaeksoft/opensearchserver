/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cache;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public abstract class LRUItemAbstract<K> implements Comparable<K> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Boolean populated = false;

	protected abstract void populate(Timer timer) throws Exception;

	final public void join(Timer timer) throws Exception {
		rwl.r.lock();
		try {
			if (populated)
				return;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (populated)
				return;
			populate(timer);
			populated = true;
		} finally {
			rwl.w.unlock();
		}
	}
}
