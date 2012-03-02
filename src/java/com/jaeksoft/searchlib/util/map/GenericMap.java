/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GenericMap<T1, T2 extends Target> {

	private TreeMap<T1, List<T2>> map;
	private List<GenericLink<T1, T2>> list;

	public GenericMap() {
		map = new TreeMap<T1, List<T2>>();
		list = null;
	}

	public List<GenericLink<T1, T2>> getList() {
		synchronized (this) {
			if (list != null)
				return list;
			list = new ArrayList<GenericLink<T1, T2>>(0);
			Iterator<Entry<T1, List<T2>>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<T1, List<T2>> entry = it.next();
				for (T2 t : entry.getValue())
					list.add(new GenericLink<T1, T2>(entry.getKey(), t));
			}
			return list;
		}
	}

	public void clear() {
		synchronized (this) {
			map.clear();
			list = null;
		}
	}

	public void copyTo(GenericMap<T1, T2> dest) {
		synchronized (this) {
			dest.clear();
			for (GenericLink<T1, T2> lnk : getList())
				dest.add(lnk.getSource(), lnk.getTarget());
		}
	}

	public void add(T1 source, T2 target) {
		synchronized (this) {
			List<T2> l = map.get(source);
			if (l == null) {
				l = new ArrayList<T2>();
				map.put(source, l);
			} else if (l.contains(target))
				return;
			l.add(target);
			list = null;
		}
	}

	public void remove(GenericLink<T1, T2> link) {
		synchronized (this) {
			T1 source = link.getSource();
			List<T2> l = map.get(source);
			if (l == null)
				return;
			l.remove(link.getTarget());
			if (l.size() == 0)
				map.remove(source);
			list = null;
		}
	}

	public List<T2> getLinks(T1 source) {
		synchronized (this) {
			return map.get(source);
		}
	}

}
