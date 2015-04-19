/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jaeksoft.searchlib.SearchLibException;

public class PropertyItem<T extends Comparable<T>> {

	private PropertyManager propertyManager;

	private List<PropertyItemListener> listeners;

	private String name;

	private T value;

	private T min;

	private T max;

	public PropertyItem(PropertyManager propertyManager, String name, T value,
			T min, T max) {
		this.propertyManager = propertyManager;
		this.name = name;
		this.value = value;
		this.listeners = null;
		this.min = min;
		this.max = max;
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
		if (value != null) {
			if (min != null)
				if (value.compareTo(min) < 0)
					value = min;
			if (max != null)
				if (value.compareTo(max) > 0)
					value = max;
			if (this.value != null && value.equals(this.value))
				return;
		}
		this.value = value;
		propertyManager.put(this);
		if (listeners != null)
			for (PropertyItemListener listener : listeners)
				listener.hasBeenSet(this);
	}

	public Boolean isValue() {
		if (value.getClass().isAssignableFrom(Boolean.class))
			return Boolean.class.cast(value);
		return false;
	}

	public void put(Properties properties) {
		properties.put(name, value.toString());
	}
}
