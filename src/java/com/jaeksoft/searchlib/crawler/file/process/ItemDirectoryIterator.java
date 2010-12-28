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

import java.net.URISyntaxException;

public class ItemDirectoryIterator extends ItemIterator {

	private FileInstanceAbstract[] files;

	private int currentPos;

	private boolean withSubDir;

	private FileInstanceAbstract fileInstance;

	protected ItemDirectoryIterator(ItemIterator parent,
			FileInstanceAbstract fileInstance, boolean withSubDir)
			throws URISyntaxException {
		super(parent);
		currentPos = -1;
		this.withSubDir = withSubDir;
		this.fileInstance = fileInstance;
		if (withSubDir)
			files = fileInstance.listFilesAndDirectories();
		else
			files = fileInstance.listFilesOnly();

	}

	@Override
	protected FileInstanceAbstract getFileInstanceImpl() {
		return fileInstance;
	}

	@Override
	protected ItemIterator nextImpl() throws URISyntaxException {
		if (files == null)
			return parent;
		if (currentPos == -1) {
			currentPos = 0;
			return this;
		}
		if (currentPos >= files.length)
			return parent;
		FileInstanceAbstract fileInstances = files[currentPos++];
		return ItemIterator.create(this, fileInstances, withSubDir);
	}

}
