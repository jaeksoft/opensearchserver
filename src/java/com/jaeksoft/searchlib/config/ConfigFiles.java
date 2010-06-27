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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigFiles {

	private TreeMap<String, ConfigFileRotation> configFiles;

	private final Lock lock = new ReentrantLock(true);

	public ConfigFiles() {
		try {
			lock.lock();
			configFiles = new TreeMap<String, ConfigFileRotation>();
		} finally {
			lock.unlock();
		}
	}

	private String getKey(File directory, String masterName) {
		return new File(directory, masterName).getAbsolutePath();
	}

	public ConfigFileRotation get(File directory, String masterName) {
		try {
			lock.lock();
			String key = getKey(directory, masterName);
			ConfigFileRotation cfr = configFiles.get(key);
			if (cfr != null)
				return cfr;
			cfr = new ConfigFileRotation(directory, masterName);
			configFiles.put(key, cfr);
			return cfr;
		} finally {
			lock.unlock();
		}
	}
}
