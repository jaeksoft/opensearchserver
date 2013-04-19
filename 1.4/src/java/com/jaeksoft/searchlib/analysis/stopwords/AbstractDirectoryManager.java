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

package com.jaeksoft.searchlib.analysis.stopwords;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AbstractDirectoryManager {

	final protected ReadWriteLock rwl = new ReadWriteLock();

	private File directory;
	private Config config;

	public AbstractDirectoryManager(Config config, File directory) {
		this.config = config;
		this.directory = directory;
	}

	private class FileOnly implements FileFilter {

		@Override
		public boolean accept(File file) {
			return file.isFile();
		}

	}

	protected Config getConfig() {
		return config;
	}

	public String[] getList(boolean addEmptyOne) {
		rwl.r.lock();
		try {
			File[] files = directory.listFiles(new FileOnly());
			if (files == null)
				return null;
			String[] list = addEmptyOne ? new String[files.length + 1]
					: new String[files.length];
			int i = 0;
			if (addEmptyOne)
				list[i++] = "";
			for (File file : files)
				list[i++] = file.getName();
			return list;
		} finally {
			rwl.r.unlock();
		}
	}

	public String[] getList() {
		return getList(false);
	}

	protected File getFile(String name) {
		return new File(directory, name);
	}

	public boolean exists(String name) {
		rwl.r.lock();
		try {
			if (name == null || name.length() == 0)
				return false;
			return getFile(name).exists();
		} finally {
			rwl.r.unlock();
		}
	}

	public void create(String name) throws IOException {
		rwl.w.lock();
		try {
			if (!directory.exists())
				directory.mkdir();
			File createFile = getFile(name);
			if (createFile.exists())
				return;
			createFile.createNewFile();
		} finally {
			rwl.w.unlock();
		}
	}

	public void delete(String name) {
		rwl.w.lock();
		try {
			File deleteFile = getFile(name);
			if (!deleteFile.exists())
				return;
			deleteFile.delete();
		} finally {
			rwl.w.unlock();
		}
	}

	public String getContent(String name) throws IOException {
		rwl.r.lock();
		try {
			return FileUtils.readFileToString(getFile(name));
		} finally {
			rwl.r.unlock();
		}
	}

	public void saveContent(String name, String content) throws IOException,
			SearchLibException {
		rwl.w.lock();
		try {
			FileUtils.writeStringToFile(getFile(name), content);
		} finally {
			rwl.w.unlock();
		}
	}
}
