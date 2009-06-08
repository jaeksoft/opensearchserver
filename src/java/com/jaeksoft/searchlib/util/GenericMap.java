/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

public class GenericMap<T> {

	private TreeMap<T, List<T>> map;
	private List<GenericLink<T>> list;

	public GenericMap() {
		map = new TreeMap<T, List<T>>();
	}

	public List<GenericLink<T>> getList() {
		synchronized (this) {
			if (list != null)
				return list;
			list = new ArrayList<GenericLink<T>>(0);
			Iterator<Entry<T, List<T>>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<T, List<T>> entry = it.next();
				for (T t : entry.getValue())
					list.add(new GenericLink<T>(entry.getKey(), t));
			}
			return list;
		}
	}

	public void add(T source, T target) {
		synchronized (this) {
			List<T> l = map.get(source);
			if (l == null) {
				l = new ArrayList<T>();
				map.put(source, l);
			} else if (l.contains(target))
				return;
			l.add(target);
			list = null;
		}
	}

	public void remove(GenericLink<T> link) {
		synchronized (this) {
			T source = link.getSource();
			List<T> l = map.get(source);
			if (l == null)
				return;
			l.remove(link.getTarget());
			if (l.size() == 0)
				map.remove(source);
			list = null;
		}
	}

	public List<T> getLinks(T source) {
		synchronized (this) {
			return map.get(source);
		}
	}

}
