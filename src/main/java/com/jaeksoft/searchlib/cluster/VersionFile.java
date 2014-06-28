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

	private RandomAccessFile writeRaf = null;

	private RandomAccessFile readRaf = null;

	private Thread writeOwner = null;

	private long version = 0;

	public final static String FILENAME = "version";

	public VersionFile(File parent) throws IOException {
		file = new File(parent, FILENAME);
		if (!file.exists())
			file.createNewFile();
	}

	public void sharedLock() throws IOException {
		if (readRaf != null)
			return;
		if (writeRaf != null) {
			if (writeOwner == Thread.currentThread())
				return;
			throw new IOException("Already open for write");
		}
		readRaf = new RandomAccessFile(file, "r");
		readRaf.getChannel().lock(0, readRaf.length(), true);
		version = readRaf.length() == 0 ? 0 : readRaf.readLong();
	}

	public long getVersion() {
		return version;
	}

	public void lock() throws IOException {
		if (writeRaf != null)
			return;
		if (readRaf != null)
			throw new IOException("Already open for read");
		writeRaf = new RandomAccessFile(file, "rw");
		writeRaf.getChannel().lock();
		version = writeRaf.length() == 0 ? 0 : writeRaf.readLong();
		writeOwner = Thread.currentThread();
	}

	public void increment() throws IOException {
		writeRaf.seek(0);
		writeRaf.writeLong(++version);
	}

	public void release() throws IOException {
		if (readRaf != null) {
			readRaf.close();
			readRaf = null;
		}
		if (writeRaf != null) {
			writeRaf.close();
			writeRaf = null;
			writeOwner = null;
		}
	}
}
