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
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.LRUMap;

public abstract class LRUCache<K extends CacheKeyInterface<K>, V> {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	protected final Lock r = rwl.readLock();
	protected final Lock w = rwl.writeLock();

	private class EvictionQueue extends LRUMap {

		private static final long serialVersionUID = -2384951296369306995L;

		protected EvictionQueue(int maxSize) {
			super(maxSize);
		}

		@Override
		protected boolean removeLRU(LinkEntry entry) {
			evictions++;
			tree.remove(entry.getKey());
			return true;
		}
	}

	private TreeMap<K, K> tree;
	private EvictionQueue queue;

	private volatile int size;
	private volatile int maxSize;
	private volatile long evictions;
	private volatile long lookups;
	private volatile long hits;
	private volatile long inserts;

	protected LRUCache(int maxSize) {
		queue = (maxSize == 0) ? null : new EvictionQueue(maxSize);
		tree = new TreeMap<K, K>();
		this.maxSize = maxSize;
		this.evictions = 0;
		this.lookups = 0;
		this.inserts = 0;
		this.hits = 0;
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	final protected V getAndPromote(K key) {
		if (queue == null)
			return null;
		r.lock();
		try {
			lookups++;
			K key2 = tree.get(key);
			if (key2 == null)
				return null;
			hits++;
			V value = (V) queue.remove(key2);
			queue.put(key2, value);
			return value;
		} finally {
			r.unlock();
		}
	}

	final public void remove(K key) {
		if (queue == null)
			return;
		w.lock();
		try {
			K key2 = tree.remove(key);
			queue.remove(key2);
			evictions++;
		} finally {
			w.unlock();
		}

	}

	final protected void put(K key, V value) {
		if (queue == null)
			return;
		w.lock();
		try {
			inserts++;
			queue.put(key, value);
			tree.put(key, key);
			size = queue.size();
		} finally {
			w.unlock();
		}
	}

	final public void clear() {
		if (queue == null)
			return;
		w.lock();
		try {
			queue.clear();
			tree.clear();
			size = queue.size();
		} finally {
			w.unlock();
		}
	}

	@Override
	final public String toString() {
		return getClass().getSimpleName() + " " + size + "/" + maxSize;
	}

	final public void xmlInfo(PrintWriter writer) {
		float hitRatio = getHitRatio();
		writer.println("<cache class=\"" + this.getClass().getName()
				+ "\" maxSize=\"" + this.maxSize + "\" size=\"" + size
				+ "\" hitRatio=\"" + hitRatio + "\" lookups=\"" + lookups
				+ "\" hits=\"" + hits + "\" inserts=\"" + inserts
				+ "\" evictions=\"" + evictions + "\">");
		writer.println("</cache>");
	}

	final public int getSize() {
		return size;
	}

	final public int getMaxSize() {
		return maxSize;
	}

	final public long getEvictions() {
		return evictions;
	}

	final public long getLookups() {
		return lookups;
	}

	final public long getHits() {
		return hits;
	}

	final public long getInserts() {
		return inserts;
	}

	final public float getHitRatio() {
		if (hits > 0 && lookups > 0)
			return (float) (((float) hits) / ((float) lookups));
		else
			return 0;
	}

	final public String getHitRatioPercent() {
		return NumberFormat.getPercentInstance().format(getHitRatio());
	}

}
