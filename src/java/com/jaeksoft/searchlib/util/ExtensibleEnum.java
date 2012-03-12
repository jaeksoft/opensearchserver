/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.List;

public class ExtensibleEnum<T extends ExtensibleEnumItem<? extends T>> {

	private List<T> list;

	public ExtensibleEnum() {
		this.list = new ArrayList<T>();
	}

	public void add(T extensibleEnumItem) {
		list.add(extensibleEnumItem);

	}

	public List<T> getList() {
		return list;
	}

	public final T getValue(String name) {
		if (name == null || list == null)
			return null;
		for (T item : list)
			if (item.getName().equals(name))
				return item;
		return null;
	}

	public T getFirst() {
		return list.get(0);
	}

}
