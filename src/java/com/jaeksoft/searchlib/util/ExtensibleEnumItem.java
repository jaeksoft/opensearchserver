/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

public abstract class ExtensibleEnumItem<T extends ExtensibleEnumItem<T>>
		implements Comparable<T> {

	public String name;

	@SuppressWarnings("unchecked")
	protected ExtensibleEnumItem(ExtensibleEnum<T> en, String name) {
		this.name = name;
		en.add((T) this);
	}

	public final String getName() {
		return name;
	}

	@Override
	public int compareTo(T o) {
		return name.compareTo(o.name);
	}
}
