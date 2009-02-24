/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.cache;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class LRUCache<K extends CacheKeyInterface<K>, V> {

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
	}

	public V getAndPromote(K key) {
		synchronized (this) {
			lookups++;
			V value = tree.get(key);
			if (value == null)
				return null;
			hits++;
			queue.remove(key);
			queue.put(key, value);
			return value;
		}
	}

	public void put(K key, V value) {
		synchronized (this) {
			inserts++;
			queue.put(key, value);
			tree.put(key, value);
		}
	}

	public void clear() {
		synchronized (this) {
			queue.clear();
			tree.clear();
		}
	}

	@Override
	public String toString() {
		synchronized (this) {
			return getClass().getSimpleName() + " " + queue.size() + "/"
					+ maxSize;
		}
	}

	public void xmlInfo(PrintWriter writer) {
		synchronized (this) {
			float hitRatio = 0;
			if (hits > 0 && lookups > 0)
				hitRatio = (float) (((float) hits) / ((float) lookups));
			writer.println("<cache class=\"" + this.getClass().getName()
					+ "\" maxSize=\"" + this.maxSize + "\" size=\""
					+ queue.size() + "\" hitRatio=\"" + hitRatio
					+ "\" lookups=\"" + lookups + "\" hits=\"" + hits
					+ "\" inserts=\"" + inserts + "\" evictions=\"" + evictions
					+ "\">");
			writer.println("</cache>");
		}
	}

}
