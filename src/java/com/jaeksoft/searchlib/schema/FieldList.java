/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FieldList<T extends Field> implements
		CacheKeyInterface<FieldList<T>>, FieldSelector, Iterable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3706856755116432969L;

	private T[] fieldArray;
	private Map<String, T> fieldMap;
	private String cacheKey;

	private ReadWriteLock rwl = new ReadWriteLock();

	/**
	 * Basic contructor.
	 */
	public FieldList() {
		this.fieldMap = new TreeMap<String, T>();
		buildCacheAndArray();
	}

	@SuppressWarnings("unchecked")
	public T[] newFieldArray(int size) {
		return (T[]) new Field[size];
	}

	/**
	 * This constructor build a new list and copy the content of the list passed
	 * as parameter (fl).
	 * 
	 * @param fl
	 */
	public FieldList(FieldList<T> fl) {
		this();
		add(fl);
	}

	public void add(FieldList<T> fl) {
		rwl.w.lock();
		try {
			for (T field : fl.getList())
				addDuplicate(field);
			buildCacheAndArray();
		} finally {
			rwl.w.unlock();
		}
	}

	private final void buildCacheAndArray() {
		fieldArray = newFieldArray(fieldMap.size());
		fieldMap.values().toArray(fieldArray);
		StringBuffer sb = new StringBuffer();
		for (Field f : fieldArray) {
			sb.append(f.name);
			sb.append('|');
		}
		cacheKey = sb.toString();
	}

	@SuppressWarnings("unchecked")
	private final void addDuplicate(T field) {
		fieldMap.put(field.name, (T) field.duplicate());
	}

	public void add(T field) {
		rwl.w.lock();
		try {
			addDuplicate(field);
			buildCacheAndArray();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Return the field with the same name
	 * 
	 * @param name
	 * @return Field
	 */
	public T get(String name) {
		rwl.r.lock();
		try {
			return fieldMap.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public T get(Field field) {
		return get(field.name);
	}

	/**
	 * Return the number of fields.
	 */
	public int size() {
		rwl.r.lock();
		try {
			return fieldArray.length;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public FieldSelectorResult accept(String fieldName) {
		rwl.r.lock();
		try {
			if (this.fieldMap.containsKey(fieldName))
				return FieldSelectorResult.LOAD;
			return FieldSelectorResult.NO_LOAD;
		} finally {
			rwl.r.unlock();
		}
	}

	public String[] toArrayName() {
		rwl.r.lock();
		try {
			Set<String> set = fieldMap.keySet();
			String[] names = new String[set.size()];
			return set.toArray(names);
		} finally {
			rwl.r.unlock();
		}
	}

	public List<T> getList() {
		rwl.r.lock();
		try {
			return Arrays.asList(fieldArray);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			for (Field f : fieldArray) {
				sb.append('[');
				sb.append(f);
				sb.append("] ");
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int compareTo(FieldList<T> o) {
		rwl.r.lock();
		try {
			return cacheKey.compareTo(o.cacheKey);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(Field field) {
		rwl.w.lock();
		try {
			fieldMap.remove(field.name);
			buildCacheAndArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void toNameList(List<String> nameList) {
		rwl.r.lock();
		try {
			for (Field field : fieldArray)
				nameList.add(field.name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			for (Field field : fieldArray)
				field.writeXmlConfig(xmlWriter);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Iterator<T> iterator() {
		rwl.r.lock();
		try {
			return Arrays.asList(fieldArray).iterator();
		} finally {
			rwl.r.unlock();
		}
	}

	public T get(int index) {
		rwl.r.lock();
		try {
			return fieldArray[index];
		} finally {
			rwl.r.unlock();
		}
	}

	public String getCacheKey() {
		rwl.r.lock();
		try {
			return cacheKey;
		} finally {
			rwl.r.unlock();
		}
	}

}
