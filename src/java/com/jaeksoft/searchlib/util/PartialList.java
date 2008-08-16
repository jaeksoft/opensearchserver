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

package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartialList<T> {

	protected List<T> list;

	public long size;

	public PartialList(int initialSize) {
		this.list = new ArrayList<T>(initialSize);
		reset();
	}

	public void reset() {
		list.clear();
		size = 0;
	}

	public Iterator<T> iterator() {
		synchronized (this) {
			return list.iterator();
		}
	}

	public long getSize() {
		synchronized (this) {
			return size;
		}
	}

	public long getRealSize() {
		synchronized (this) {
			return list.size();
		}
	}

	public T get(long index) {
		synchronized (this) {
			return list.get((int) index);
		}
	}

	public void add(T element) {
		synchronized (this) {
			list.add(element);
		}
	}

	public void set(List<T> list, long size) {
		synchronized (this) {
			this.size = size;
			this.list = list;
		}
	}

}
