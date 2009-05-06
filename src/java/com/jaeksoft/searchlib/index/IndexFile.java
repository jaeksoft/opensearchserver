/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IndexFile {

	private File file;
	private FileOutputStream fos;

	public IndexFile(File parentDir, String indexRef, String fileName) {
		File indexDir = new File(parentDir, indexRef);
		if (!indexDir.exists())
			indexDir.mkdir();
		this.file = new File(indexDir, fileName);
	}

	protected void writeBuffer(byte[] buffer, int length) throws IOException {
		fos.write(buffer, 0, length);
	}

	public void put(InputStream is) throws IOException {
		fos = new FileOutputStream(file);
		byte[] buffer = new byte[65536];
		int l;
		while ((l = is.read(buffer)) != -1)
			writeBuffer(buffer, l);
		fos.close();
	}
}
