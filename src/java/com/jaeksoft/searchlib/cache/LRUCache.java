/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cache;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class LRUCache<K extends CacheKeyInterface<K>, V> {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	protected final Lock r = rwl.readLock();
	protected final Lock w = rwl.writeLock();

	private class EvictionQueue extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -2384951296369306995L;

		public EvictionQueue(int maxSize) {
			super(maxSize);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if (size() <= maxSize)
				return false;
			tree.remove(eldest.getKey());
			evictions++;
			return true;
		}
	}

	private TreeMap<K, V> tree;
	private EvictionQueue queue;

	private int size;
	private int maxSize;
	private long evictions;
	private long lookups;
	private long hits;
	private long inserts;

	protected LRUCache(int maxSize) {
		queue = new EvictionQueue(maxSize);
		tree = new TreeMap<K, V>();
		this.maxSize = maxSize;
		this.evictions = 0;
		this.lookups = 0;
		this.inserts = 0;
		this.hits = 0;
		this.size = 0;
	}

	final protected V getAndPromote(K key) {
		r.lock();
		try {
			lookups++;
			V value = tree.get(key);
			if (value == null)
				return null;
			hits++;
			queue.remove(key);
			queue.put(key, value);
			return value;
		} finally {
			r.unlock();
		}
	}

	final public void remove(K key) {
		w.lock();
		try {
			queue.remove(key);
			tree.remove(key);
			evictions++;
		} finally {
			w.unlock();
		}

	}

	final protected void putNoLock(K key, V value) {
		inserts++;
		queue.put(key, value);
		tree.put(key, value);
		size = queue.size();

	}

	final protected void put(K key, V value) {
		w.lock();
		try {
			putNoLock(key, value);
		} finally {
			w.unlock();
		}
	}

	final public void clear() {
		w.lock();
		try {
			queue.clear();
			tree.clear();
			size = queue.size();
		} finally {
			w.unlock();
		}
	}

	final public Map<K, V> getMap() {
		r.lock();
		try {
			return tree;
		} finally {
			r.unlock();
		}
	}

	@Override
	final public String toString() {
		r.lock();
		try {
			return getClass().getSimpleName() + " " + queue.size() + "/"
					+ maxSize;
		} finally {
			r.unlock();
		}
	}

	final public void xmlInfo(PrintWriter writer) {
		r.lock();
		try {
			float hitRatio = getHitRatio();
			writer.println("<cache class=\"" + this.getClass().getName()
					+ "\" maxSize=\"" + this.maxSize + "\" size=\""
					+ queue.size() + "\" hitRatio=\"" + hitRatio
					+ "\" lookups=\"" + lookups + "\" hits=\"" + hits
					+ "\" inserts=\"" + inserts + "\" evictions=\"" + evictions
					+ "\">");
			writer.println("</cache>");
		} finally {
			r.unlock();
		}
	}

	final public int getSize() {
		r.lock();
		try {
			return size;
		} finally {
			r.unlock();
		}
	}

	final public int getMaxSize() {
		r.lock();
		try {
			return maxSize;
		} finally {
			r.unlock();
		}
	}

	final public long getEvictions() {
		r.lock();
		try {
			return evictions;
		} finally {
			r.unlock();
		}
	}

	final public long getLookups() {
		r.lock();
		try {
			return lookups;
		} finally {
			r.unlock();
		}
	}

	final public long getHits() {
		r.lock();
		try {
			return hits;
		} finally {
			r.unlock();
		}
	}

	final public long getInserts() {
		r.lock();
		try {
			return inserts;
		} finally {
			r.unlock();
		}
	}

	final public float getHitRatio() {
		r.lock();
		try {
			if (hits > 0 && lookups > 0)
				return (float) (((float) hits) / ((float) lookups));
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	final public String getHitRatioPercent() {
		return NumberFormat.getPercentInstance().format(getHitRatio());
	}

}
