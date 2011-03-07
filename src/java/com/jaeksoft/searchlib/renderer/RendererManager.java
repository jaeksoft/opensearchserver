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

package com.jaeksoft.searchlib.renderer;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class RendererManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Renderer[] array;

	private TreeSet<Renderer> set;

	public RendererManager(Config config, File directory)
			throws SearchLibException, XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		array = null;
		set = new TreeSet<Renderer>();
		for (File f : directory.listFiles()) {
			if (f.isFile()) {
				String fname = f.getName();
				if (!FilenameUtils.isExtension(fname, "xml"))
					continue;
				if (fname.endsWith("_old.xml"))
					continue;
				if (fname.endsWith("_tmp.xml"))
					continue;
				add(new Renderer(f));
			}
		}
	}

	private void buildArray() {
		array = new Renderer[set.size()];
		set.toArray(array);
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
			if (set.contains(item))
				throw new SearchLibException("This item already exists");
			set.add(item);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void replace(Renderer oldItem, Renderer newItem) {
		rwl.w.lock();
		try {
			set.remove(oldItem);
			set.add(newItem);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(Renderer item) {
		rwl.w.lock();
		try {
			set.remove(item);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

}
