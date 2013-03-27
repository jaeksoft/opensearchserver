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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class AbstractFieldList<T extends AbstractField<T>> implements
		CacheKeyInterface<AbstractFieldList<T>>, Iterable<T> {

	private List<T> fieldList;
	private Map<String, T> fieldMap;
	private String cacheKey;

	private ReadWriteLock rwl = new ReadWriteLock();

	/**
	 * Basic contructor.
	 */
	public AbstractFieldList() {
		this.fieldMap = new TreeMap<String, T>();
		this.fieldList = new ArrayList<T>(0);
		buildCacheKey();
	}

	public void clear() {
		this.fieldList.clear();
		this.fieldMap.clear();
		buildCacheKey();
	}

	/**
	 * This constructor build a new list and copy the content of the list passed
	 * as parameter (fl).
	 * 
	 * @param fl
	 */
	public AbstractFieldList(AbstractFieldList<T> fl) {
		this();
		add(fl);
	}

	public void add(AbstractFieldList<T> fl) {
		rwl.w.lock();
		try {
			for (T field : fl)
				addNoLockNoCache(field.duplicate());
			buildCacheKey();
		} finally {
			rwl.w.unlock();
		}
	}

	private final void buildCacheKey() {
		StringBuffer sb = new StringBuffer();
		for (T f : fieldMap.values()) {
			sb.append(f.toString());
			sb.append('|');
		}
		cacheKey = sb.toString();
	}

	private void addNoLockNoCache(T field) {
		T previousField = fieldMap.put(field.name, field);
		if (previousField != null)
			fieldList.remove(previousField);
		fieldList.add(field);
	}

	public void put(T field) {
		rwl.w.lock();
		try {
			addNoLockNoCache(field);
			buildCacheKey();
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

	public T get(AbstractField<?> field) {
		return get(field.name);
	}

	/**
	 * Return the number of fields.
	 */
	public int size() {
		rwl.r.lock();
		try {
			return fieldList.size();
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
			return fieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			for (T f : fieldList) {
				sb.append('[');
				sb.append(f.toString());
				sb.append("] ");
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int compareTo(AbstractFieldList<T> o) {
		rwl.r.lock();
		try {
			return cacheKey.compareTo(o.cacheKey);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(String fieldName) {
		rwl.w.lock();
		try {
			T field = fieldMap.remove(fieldName);
			fieldList.remove(field);
			buildCacheKey();
		} finally {
			rwl.w.unlock();
		}
	}

	public void toNameList(List<String> nameList) {
		rwl.r.lock();
		try {
			for (String fieldName : fieldMap.keySet())
				nameList.add(fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			for (T field : fieldList)
				field.writeXmlConfig(xmlWriter);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Iterator<T> iterator() {
		rwl.r.lock();
		try {
			return fieldList.iterator();
		} finally {
			rwl.r.unlock();
		}
	}

	public T get(int index) {
		rwl.r.lock();
		try {
			return fieldList.get(index);
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

	public final void populate(Set<String> fieldNameSet) {
		for (String fieldName : fieldMap.keySet())
			fieldNameSet.add(fieldName);
	}

}
