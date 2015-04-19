/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class LockedMap<K, V> implements Map<K, V> {

	private final ReadWriteLock rwl;
	private final Map<K, V> map;

	public LockedMap(Map<K, V> map) {
		rwl = new ReadWriteLock();
		this.map = map;
	}

	@Override
	public void clear() {
		rwl.w.lock();
		try {
			map.clear();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		rwl.r.lock();
		try {
			return map.containsKey(key);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean containsValue(Object value) {
		rwl.r.lock();
		try {
			return map.containsValue(value);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		rwl.r.lock();
		try {
			return map.entrySet();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public V get(Object key) {
		rwl.r.lock();
		try {
			return map.get(key);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		rwl.r.lock();
		try {
			return map.isEmpty();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Set<K> keySet() {
		rwl.r.lock();
		try {
			return map.keySet();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public V put(K key, V value) {
		rwl.w.lock();
		try {
			return map.put(key, value);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		rwl.w.lock();
		try {
			map.putAll(m);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public V remove(Object key) {
		rwl.w.lock();
		try {
			return map.remove(key);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public int size() {
		rwl.r.lock();
		try {
			return map.size();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Collection<V> values() {
		rwl.r.lock();
		try {
			return map.values();
		} finally {
			rwl.r.unlock();
		}
	}

	public List<V> valueList() {
		rwl.r.lock();
		try {
			return new ArrayList<V>(map.values());
		} finally {
			rwl.r.unlock();
		}
	}

}
