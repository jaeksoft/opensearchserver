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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class UniqueNameSetGeneric<T extends UniqueNameItem<T>> {

	private TreeSet<T> set;

	private T[] array;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	protected void init() {
		set = new TreeSet<T>();
		array = null;
	}

	public void add(T item) {
		w.lock();
		try {
			set.add(item);
			array = null;
		} finally {
			w.unlock();
		}
	}

	public void remove(T item) {
		w.lock();
		try {
			set.remove(item);
			array = null;
		} finally {
			w.unlock();
		}
	}

	protected abstract T[] newArray(int size);

	protected abstract T newItem(String name);

	public T[] getArray() {
		r.lock();
		try {
			if (array != null)
				return array;
			array = newArray(set.size());
			set.toArray(array);
			return array;
		} finally {
			r.unlock();
		}
	}

	public T get(String name) {
		r.lock();
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
			r.unlock();
		}
	}

	public void writeXml(String elementName, XmlWriter xmlWriter)
			throws SAXException {
		r.lock();
		try {
			xmlWriter.startElement(elementName);
			for (T item : set)
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}

}
