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

import java.util.List;

public abstract class ExtensibleEnum<T> implements
		Comparable<ExtensibleEnum<T>> {

	public String name;

	@SuppressWarnings("unchecked")
	protected ExtensibleEnum(String name) {
		this.name = name;
		getValues().add((T) this);
	}

	protected abstract List<T> getValues();

	public final String getName() {
		return name;
	}

	@Override
	public int compareTo(ExtensibleEnum<T> o) {
		return name.compareTo(o.name);
	}

	public final static ExtensibleEnum<?> getValue(
			List<? extends ExtensibleEnum<?>> list, String name) {
		if (name == null || list == null)
			return null;
		for (ExtensibleEnum<?> item : list)
			if (name.equals(item.name))
				return item;
		return null;
	}
}
