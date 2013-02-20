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

public abstract class ExtensibleEnumItem<T extends ExtensibleEnumItem<? extends T>>
		implements Comparable<T> {

	protected String name;

	@SuppressWarnings("unchecked")
	protected ExtensibleEnumItem(ExtensibleEnum<T> en, String name) {
		setName(name);
		en.add((T) this);
	}

	protected void setName(String name) {
		this.name = name;

	}

	public final String getName() {
		return name;
	}

	@Override
	public int compareTo(T o) {
		return name.compareTo(o.name);
	}
}
