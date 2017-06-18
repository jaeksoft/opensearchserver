/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web;

import com.jaeksoft.searchlib.SearchLibException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class GenericCache<K, V extends GenericCache.Expirable> {

	public interface Expirable {

		long getExpirationTime();

		boolean isCacheable();
	}

	public interface ItemSupplier<R extends Expirable> {

		R get() throws SearchLibException, URISyntaxException, IOException;
	}

	private final Map<K, V> map;
	private final Map<K, Object> keyLock;

	protected GenericCache() {
		map = new HashMap<>();
		keyLock = new HashMap<>();
	}

	/**
	 * Remove the expired items (relative to the t parameter)
	 *
	 * @param t
	 */
	private void checkExpiration(long t) {
		synchronized (map) {
			final Iterator<Entry<K, V>> it = map.entrySet().iterator();
			ArrayList<K> keyToRemove = null;
			while (it.hasNext()) {
				Entry<K, V> e = it.next();
				if (t > e.getValue().getExpirationTime()) {
					if (keyToRemove == null)
						keyToRemove = new ArrayList<>();
					keyToRemove.add(e.getKey());
				}

			}
			if (keyToRemove != null) {
				for (K key : keyToRemove) {
					map.remove(key);
					keyLock.remove(key);
				}
			}
		}
	}

	/**
	 * Return the RobotsTxt object related to the URL.
	 *
	 * @param key
	 * @param supplier
	 * @param forceReload
	 * @return
	 * @throws SearchLibException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	final public V getOrCreate(K key, boolean forceReload, ItemSupplier<V> supplier)
			throws SearchLibException, URISyntaxException, IOException {
		Object lock;
		synchronized (map) {
			lock = keyLock.get(key);
			if (lock == null) {
				lock = new Object();
				keyLock.put(key, lock);
			}
			checkExpiration(System.currentTimeMillis());
			if (forceReload)
				map.remove(key);
			V value = map.get(key);
			if (value != null)
				return value;
		}
		synchronized (lock) {
			synchronized (map) {
				V value = map.get(key);
				if (value != null)
					return value;
			}
			V value = supplier.get();
			synchronized (map) {
				map.put(key, value);
				return value;
			}
		}
	}

	final public V get(K key) {
		synchronized (map) {
			return map.get(key);
		}
	}

	protected abstract V[] newArray(int size);

	final public V[] getList() {
		synchronized (map) {
			return map.values().toArray(newArray(map.size()));
		}
	}

}
