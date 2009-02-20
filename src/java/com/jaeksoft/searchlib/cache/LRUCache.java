/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

public class LRUCache<K, V> {

	private class CacheMap extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -2384951296369306995L;

		public CacheMap(int maxSize) {
			super(maxSize);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if (size() <= maxSize)
				return false;
			evictions++;
			return true;
		}
	}

	private CacheMap map;
	private int maxSize;
	private long evictions;
	private long lookups;
	private long hits;
	private long inserts;
	private long memoryEvictions;

	protected LRUCache(int maxSize) {
		map = new CacheMap(maxSize);
		this.maxSize = maxSize;
		this.evictions = 0;
		this.lookups = 0;
		this.inserts = 0;
		this.hits = 0;
		this.memoryEvictions = 0;
	}

	public V getAndPromote(K key) {
		synchronized (this) {
			lookups++;
			V value = map.get(key);
			if (value == null)
				return null;
			hits++;
			map.remove(key);
			map.put(key, value);
			return value;
		}
	}

	public void expire(K key) {
		synchronized (this) {
			evictions++;
			map.remove(key);
		}
	}

	public V put(K key, V value) {
		synchronized (this) {
			inserts++;
			return map.put(key, value);
		}
	}

	public void clear() {
		synchronized (this) {
			map.clear();
		}
	}

	@Override
	public String toString() {
		synchronized (this) {
			return getClass().getSimpleName() + " " + map.size() + "/"
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
					+ map.size() + "\" hitRatio=\"" + hitRatio
					+ "\" lookups=\"" + lookups + "\" hits=\"" + hits
					+ "\" inserts=\"" + inserts + "\" evictions=\"" + evictions
					+ "\" memoryEviction=\"" + memoryEvictions + "\">");
			writer.println("</cache>");
		}
	}

}
