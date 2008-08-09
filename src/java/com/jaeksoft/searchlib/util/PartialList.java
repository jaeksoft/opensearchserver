/**   
 * License Agreement for Jaeksoft Pojodbc
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft Pojodbc.
 *
 * Jaeksoft Pojodbc is free software: you can redistribute it and/or
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
 *  along with Jaeksoft Pojodbc. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public abstract class PartialList<T> extends AbstractList<T> {

	protected int size;
	protected List<T> partialList;
	protected int currentStart;
	protected int windowRows;

	public PartialList(int windowRows) {
		this.windowRows = windowRows;
		this.currentStart = 0;
		this.partialList = null;
		this.size = 0;
	}

	@Override
	public T get(int index) {
		synchronized (this) {
			if (index < currentStart || index >= currentStart + windowRows) {
				update(index);
				currentStart = index;
			}
			return partialList.get(index - currentStart);
		}
	}

	public void setNewList(List<T> list, int size) {
		synchronized (this) {
			this.size = size;
			this.partialList = list;
		}
	}

	@Override
	public int size() {
		synchronized (this) {
			return size;
		}
	}

	public Iterator<T> iterator() {
		return partialList.iterator();
	}

	protected abstract void update(int start);

}
