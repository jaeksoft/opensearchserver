/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jaeksoft.searchlib.SearchLibException;

public class PropertyItem<T> {

	private PropertyManager propertyManager;

	private List<PropertyItemListener> listeners;

	private String name;

	private T value;

	public PropertyItem(PropertyManager propertyManager, String name, T value) {
		this.propertyManager = propertyManager;
		this.name = name;
		this.value = value;
		this.listeners = null;
	}

	public void addListener(PropertyItemListener listener) {
		if (listeners == null)
			listeners = new ArrayList<PropertyItemListener>(1);
		listeners.add(listener);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getValue() {
		return value;
	}

	public void initValue(T value) {
		this.value = value;
	}

	public void setValue(T value) throws IOException, SearchLibException {
		if (value != null && this.value != null)
			if (value.equals(this.value))
				return;
		this.value = value;
		propertyManager.put(this);
		if (listeners != null)
			for (PropertyItemListener listener : listeners)
				listener.hasBeenSet(this);
	}

	public boolean isValue() {
		if (value instanceof Boolean)
			return (Boolean) value;
		return false;
	}

	public void put(Properties properties) {
		properties.put(name, value.toString());
	}
}
