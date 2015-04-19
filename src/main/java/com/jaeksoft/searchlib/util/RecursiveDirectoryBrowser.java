/**   
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

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.FileFilter;

import com.jaeksoft.searchlib.SearchLibException;

public class RecursiveDirectoryBrowser {

	private CallBack callBack;

	private SearchLibException searchLibException;

	public interface CallBack {
		void file(File file) throws SearchLibException;
	}

	public RecursiveDirectoryBrowser(File file, CallBack callBack)
			throws SearchLibException {
		searchLibException = null;
		this.callBack = callBack;
		if (!file.isDirectory())
			return;
		file.listFiles(new _FileFilter());
		if (searchLibException != null)
			throw searchLibException;
	}

	private class _FileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			if (searchLibException != null)
				return false;
			if (file.getName().startsWith("."))
				return false;
			try {
				callBack.file(file);
			} catch (SearchLibException e) {
				searchLibException = e;
				return false;
			}
			file.listFiles(this);
			return true;
		}
	}
}
