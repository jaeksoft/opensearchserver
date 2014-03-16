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

package com.jaeksoft.searchlib.cluster;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class VersionFile {

	private final File file;

	private RandomAccessFile raf = null;

	private long version = 0;

	public final static String FILENAME = "version";

	public VersionFile(File parent) throws IOException {
		file = new File(parent, FILENAME);
		if (!file.exists())
			file.createNewFile();
	}

	public long readVersion() throws IOException {
		RandomAccessFile localRaf = raf;
		if (localRaf == null) {
			localRaf = new RandomAccessFile(file, "r");
			localRaf.getChannel().lock(0, raf.length(), true);
		} else
			raf.seek(0);
		try {
			version = localRaf.length() == 0 ? 0 : raf.readLong();
			return version;
		} finally {
			if (localRaf != raf)
				localRaf.close();
		}
	}

	public void lock() throws IOException {
		if (raf != null)
			throw new IOException("Already open");
		raf = new RandomAccessFile(file, "rw");
		raf.getChannel().lock();
		version = raf.length() == 0 ? 0 : raf.readLong();
	}

	public void increment() throws IOException {
		raf.seek(0);
		raf.writeLong(++version);
	}

	public void release() throws IOException {
		if (raf != null) {
			raf.close();
			raf = null;
		}
	}
}
