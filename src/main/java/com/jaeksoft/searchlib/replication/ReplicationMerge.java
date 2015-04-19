/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.replication;

import java.io.File;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.RecursiveDirectoryBrowser;

public class ReplicationMerge implements RecursiveDirectoryBrowser.CallBack {

	private final int prefixSize;
	private final File destRoot;

	public ReplicationMerge(File srcRoot, File destRoot)
			throws SearchLibException {
		this.prefixSize = srcRoot.getAbsolutePath().length();
		this.destRoot = destRoot;
		new RecursiveDirectoryBrowser(srcRoot, this);
	}

	@Override
	public void file(File file) throws SearchLibException {
		File dest = new File(destRoot, file.getAbsolutePath().substring(
				prefixSize));
		if (dest.exists())
			dest.delete();
		else {
			File parent = dest.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
		}
		file.renameTo(dest);
	}
}
