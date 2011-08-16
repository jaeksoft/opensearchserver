/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.synonym;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.stopwords.AbstractDirectoryManager;
import com.jaeksoft.searchlib.config.Config;

public class SynonymsManager extends AbstractDirectoryManager {

	private TreeMap<String, SynonymMap> synonymMaps;

	public SynonymsManager(Config config, File directory) {
		super(config, directory);
		synonymMaps = new TreeMap<String, SynonymMap>();
	}

	private SynonymMap getNewSynonymMap(String listname)
			throws SearchLibException {
		try {
			return new SynonymMap(getFile(listname));
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public SynonymMap getSynonyms(String listname) throws SearchLibException {
		rwl.r.lock();
		try {
			SynonymMap synonymMap = synonymMaps.get(listname);
			if (synonymMap != null)
				return synonymMap;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			SynonymMap synonymMap = synonymMaps.get(listname);
			if (synonymMap != null)
				return synonymMap;
			synonymMap = getNewSynonymMap(listname);
			if (synonymMap != null)
				synonymMaps.put(listname, synonymMap);
			return synonymMap;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void delete(String name) {
		rwl.w.lock();
		try {
			super.delete(name);
			synonymMaps.remove(name);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void saveContent(String name, String content) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			super.saveContent(name, content);
			synonymMaps.remove(name);
			synonymMaps.put(name, getSynonyms(name));
		} finally {
			rwl.w.unlock();
		}
	}
}
