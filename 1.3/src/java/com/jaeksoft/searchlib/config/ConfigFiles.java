/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.util.TreeMap;

import com.jaeksoft.searchlib.util.SimpleLock;

public class ConfigFiles {

	private TreeMap<String, ConfigFileRotation> configFiles;

	private final SimpleLock lock = new SimpleLock();

	public ConfigFiles() {
		lock.rl.lock();
		try {
			configFiles = new TreeMap<String, ConfigFileRotation>();
		} finally {
			lock.rl.unlock();
		}
	}

	private String getKey(File directory, String masterName) {
		return new File(directory, masterName).getAbsolutePath();
	}

	public ConfigFileRotation get(File directory, String masterName) {
		lock.rl.lock();
		try {
			String key = getKey(directory, masterName);
			ConfigFileRotation cfr = configFiles.get(key);
			if (cfr != null)
				return cfr;
			cfr = new ConfigFileRotation(directory, masterName);
			configFiles.put(key, cfr);
			return cfr;
		} finally {
			lock.rl.unlock();
		}
	}
}
