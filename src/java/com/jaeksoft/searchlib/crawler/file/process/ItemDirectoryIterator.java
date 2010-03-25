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

package com.jaeksoft.searchlib.crawler.file.process;

import java.io.File;
import java.io.FileFilter;

public class ItemDirectoryIterator extends ItemIterator {

	private class FileOnlyFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile();
		}

	}

	private File[] files;

	private int currentPos;

	private boolean withSubDir;

	protected ItemDirectoryIterator(ItemIterator parent, File file,
			boolean withSubDir) {
		super(parent);
		currentPos = 0;
		this.withSubDir = withSubDir;
		if (withSubDir)
			files = file.listFiles();
		else
			files = file.listFiles(new FileOnlyFilter());

	}

	@Override
	protected File getFileImpl() {
		return null;
	}

	@Override
	protected ItemIterator nextImpl() {
		if (files == null)
			return null;
		if (currentPos >= files.length)
			return null;
		File file = files[currentPos++];
		return ItemIterator.create(this, file, withSubDir);
	}

}
