/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class YoutubeItemCache extends LinkedHashMap<String, YoutubeItem> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 2452704318102895191L;

	private final int maxEntries;

	private final static YoutubeItemCache cache = new YoutubeItemCache(1000);

	private final static ReadWriteLock rwl = new ReadWriteLock();

	private YoutubeItemCache(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<String, YoutubeItem> eldest) {
		return size() > maxEntries;
	}

	public static void addItem(String key, YoutubeItem item) {
		rwl.w.lock();
		try {
			cache.put(key, item);
		} finally {
			rwl.w.unlock();
		}
	}

	public static YoutubeItem getItem(String key) {
		rwl.r.lock();
		try {
			return cache.get(key);
		} finally {
			rwl.r.unlock();
		}

	}
}
