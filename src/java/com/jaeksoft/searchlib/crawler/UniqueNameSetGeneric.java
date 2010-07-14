/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import java.util.SortedSet;
import java.util.TreeSet;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class UniqueNameSetGeneric<T extends UniqueNameItem<T>> {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeSet<T> set;

	private T[] array;

	protected void init() {
		set = new TreeSet<T>();
		array = null;
	}

	public void add(T item) {
		rwl.w.lock();
		try {
			set.add(item);
			array = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(T item) {
		rwl.w.lock();
		try {
			set.remove(item);
			array = null;
		} finally {
			rwl.w.unlock();
		}
	}

	protected abstract T[] newArray(int size);

	protected abstract T newItem(String name);

	public T[] getArray() {
		rwl.r.lock();
		try {
			if (array != null)
				return array;
			array = newArray(set.size());
			set.toArray(array);
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

	public T get(String name) {
		rwl.r.lock();
		try {
			T finder = newItem(name);
			finder.setName(name);
			SortedSet<T> s = set.subSet(finder, true, finder, true);
			if (s == null)
				return null;
			if (s.size() == 0)
				return null;
			return s.first();
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(String elementName, XmlWriter xmlWriter)
			throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(elementName);
			for (T item : set)
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

}
