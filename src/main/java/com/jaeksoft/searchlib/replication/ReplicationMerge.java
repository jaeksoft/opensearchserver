/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
import java.io.IOException;

import org.apache.commons.io.FileUtils;

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
		if (dest.exists()) {
			if (!dest.delete())
				throw new SearchLibException("Unable to delete the file: "
						+ dest.getAbsolutePath());
		} else {
			File parent = dest.getParentFile();
			if (!parent.exists())
				if (!parent.mkdirs())
					throw new SearchLibException(
							"Unable to create the directory: "
									+ parent.getAbsolutePath());
		}
		if (!file.isFile())
			throw new SearchLibException("Unsupported file type: "
					+ file.getAbsolutePath());
		try {
			FileUtils.moveFile(file, dest);
		} catch (IOException e) {
			throw new SearchLibException("File move failed on "
					+ file.getAbsolutePath());
		}
	}
}
