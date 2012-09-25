/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;

public class RendererManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Renderer[] array;

	private TreeMap<String, Renderer> map;

	public RendererManager(Config config, File directory)
			throws SearchLibException, XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		array = null;
		map = new TreeMap<String, Renderer>();
		for (File f : directory.listFiles()) {
			if (f.isFile()) {
				String fname = f.getName();
				if (!FilenameUtils.isExtension(fname, "xml"))
					continue;
				if (fname.endsWith("_old.xml"))
					continue;
				if (fname.endsWith("_tmp.xml"))
					continue;
				if (f.exists())
					add(new Renderer(new XPathParser(f)));
				else
					add(new Renderer());
			}
		}
	}

	private void buildArray() {
		array = new Renderer[map.size()];
		int i = 0;
		for (Map.Entry<String, Renderer> entry : map.entrySet())
			array[i++] = entry.getValue();
	}

	public Renderer[] getArray() {
		rwl.r.lock();
		try {
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(Renderer item) throws SearchLibException {
		rwl.w.lock();
		try {
			if (map.containsKey(item.getName()))
				throw new SearchLibException("This item already exists");
			map.put(item.getName(), item);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public Renderer get(String rendererName) {
		rwl.r.lock();
		try {
			return map.get(rendererName);
		} finally {
			rwl.r.unlock();
		}
	}

	public void replace(Renderer oldItem, Renderer newItem) {
		rwl.w.lock();
		try {
			map.remove(oldItem.getName());
			map.put(newItem.getName(), newItem);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(Renderer item) {
		rwl.w.lock();
		try {
			map.remove(item.getName());
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

}
