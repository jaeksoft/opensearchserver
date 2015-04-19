/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public abstract class LRUCache<K extends LRUItemAbstract<K>> {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<K, K> cacheMap;
	private EvictionQueue evictionQueue;

	private final String name;

	private int maxSize;
	private long evictions;
	private long lookups;
	private long hits;
	private long inserts;

	private class EvictionQueue extends LinkedHashMap<K, K> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2876891913920705107L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, K> eldest) {
			if (size() <= maxSize)
				return false;
			cacheMap.remove(eldest.getKey());
			evictions++;
			return true;
		}
	}

	protected LRUCache(String name, int maxSize) {
		this.name = name;
		this.evictions = 0;
		this.lookups = 0;
		this.inserts = 0;
		this.hits = 0;
		this.maxSize = 0;
		this.cacheMap = null;
		this.evictionQueue = null;
		setMaxSize_noLock(maxSize);
	}

	private void setMaxSize_noLock(int newMaxSize) {
		if (newMaxSize == maxSize)
			return;
		if (newMaxSize == 0) {
			clear_nolock();
			cacheMap = null;
			evictionQueue = null;
		} else {
			if (newMaxSize < maxSize) {
				cacheMap = null;
				evictionQueue = null;
			}
			if (cacheMap == null)
				cacheMap = new TreeMap<K, K>();
			if (evictionQueue == null)
				evictionQueue = new EvictionQueue();
		}
		maxSize = newMaxSize;
	}

	public void setMaxSize(int newMaxSize) {
		rwl.w.lock();
		try {
			setMaxSize_noLock(newMaxSize);
		} finally {
			rwl.w.unlock();
		}
	}

	final protected K getAndPromote(final K newItem) {
		rwl.w.lock();
		try {
			if (cacheMap == null)
				return newItem;
			lookups++;
			K prevItem = cacheMap.get(newItem);
			if (prevItem != null) {
				evictionQueue.remove(prevItem);
				evictionQueue.put(prevItem, prevItem);
				hits++;
				return prevItem;
			}
			evictionQueue.put(newItem, newItem);
			cacheMap.put(newItem, newItem);
			inserts++;
			return newItem;
		} finally {
			rwl.w.unlock();
		}
	}

	final public void put(final K item) {
		rwl.w.lock();
		try {
			if (cacheMap == null)
				return;
			evictionQueue.put(item, item);
			cacheMap.put(item, item);
			inserts++;
		} finally {
			rwl.w.unlock();
		}
	}

	final public boolean remove(final K key) {
		rwl.w.lock();
		try {
			if (cacheMap == null)
				return false;
			K item1 = cacheMap.remove(key);
			K item2 = evictionQueue.remove(key);
			if (item1 == null && item2 == null)
				return false;
			evictions++;
			return true;
		} finally {
			rwl.w.unlock();
		}
	}

	public K getAndJoin(K item, Timer timer) throws Exception {
		item = getAndPromote(item);
		item.join(timer);
		return item;
	}

	final private void clear_nolock() {
		if (cacheMap != null)
			cacheMap.clear();
		if (evictionQueue != null)
			evictionQueue.clear();
	}

	final public void clear() {
		rwl.w.lock();
		try {
			clear_nolock();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	final public String toString() {
		rwl.r.lock();
		try {
			return StringUtils
					.fastConcat(
							name,
							" - Size: ",
							cacheMap == null ? 0 : Integer.toString(cacheMap
									.size()),
							'/',
							evictionQueue == null ? 0 : Integer
									.toString(evictionQueue.size())
									+ " - MaxSize: ",
							Integer.toString(maxSize), " - Lookup: ", Long
									.toString(lookups), " - Insert: ", Long
									.toString(inserts), " HitRatio: ",
							getHitRatioPercent());
		} finally {
			rwl.r.unlock();
		}
	}

	final public void xmlInfo(PrintWriter writer) {
		rwl.r.lock();
		try {
			float hitRatio = getHitRatio();
			writer.println("<cache class=\"" + this.getClass().getName()
					+ "\" maxSize=\"" + this.maxSize + "\" size=\"" + cacheMap == null ? "0"
					: cacheMap.size() + "\" hitRatio=\"" + hitRatio
							+ "\" lookups=\"" + lookups + "\" hits=\"" + hits
							+ "\" inserts=\"" + inserts + "\" evictions=\""
							+ evictions + "\">");
			writer.println("</cache>");
		} finally {
			rwl.r.unlock();
		}
	}

	final public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	final public int getSize() {
		rwl.r.lock();
		try {
			return cacheMap == null ? 0 : cacheMap.size();
		} finally {
			rwl.r.unlock();
		}
	}

	final public int getMaxSize() {
		rwl.r.lock();
		try {
			return maxSize;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getEvictions() {
		rwl.r.lock();
		try {
			return evictions;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getLookups() {
		rwl.r.lock();
		try {
			return lookups;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getHits() {
		rwl.r.lock();
		try {
			return hits;
		} finally {
			rwl.r.unlock();
		}
	}

	final public long getInserts() {
		rwl.r.lock();
		try {
			return inserts;
		} finally {
			rwl.r.unlock();
		}
	}

	final public float getHitRatio() {
		rwl.r.lock();
		try {
			if (hits > 0 && lookups > 0)
				return (float) (((float) hits) / ((float) lookups));
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	final public String getHitRatioPercent() {
		return NumberFormat.getPercentInstance().format(getHitRatio());
	}

}
