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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import com.jaeksoft.searchlib.util.IOUtils;

public class LockableFile {

	private final File file;
	private FileLock fileLock;
	private FileInputStream fileInputStream;
	private FileOutputStream fileOutputStream;

	/**
	 * Create a lockable file instance.
	 * 
	 * @param parent
	 * @param child
	 * @param createIfNotExist
	 * @throws IOException
	 */
	public LockableFile(File parent, String child, boolean createIfNotExist)
			throws IOException {
		file = new File(parent, child);
		if (createIfNotExist)
			createIfNotExist();
	}

	/**
	 * Create the file if it does not exist.
	 * 
	 * @throws IOException
	 */
	public void createIfNotExist() throws IOException {
		synchronized (file) {
			if (file.exists())
				return;
			file.createNewFile();
		}
	}

	/**
	 * Create a new FileInputStream with a shared lock. If a lock already
	 * exists, an IOException is returned.
	 * 
	 * @return a new FileInputStream
	 * @throws IOException
	 */
	public FileInputStream getFileInputStream() throws IOException {
		synchronized (file) {
			if (fileInputStream != null)
				throw new IOException("A file input stream already exists");
			if (fileLock != null)
				throw new IOException("Alraady (file) locked");
			fileInputStream = new FileInputStream(file);
			fileLock = fileInputStream.getChannel().lock(0, file.length(),
					false);
			return fileInputStream;
		}
	}

	public FileOutputStream getFileOutputStream() throws IOException {
		if (fileOutputStream != null)
			throw new IOException("A file output stream already exists");
		if (fileLock != null)
			throw new IOException("Alraady (file) locked");
		fileOutputStream = new FileOutputStream(file);
		fileLock = fileOutputStream.getChannel().lock();
		return fileOutputStream;

	}

	/**
	 * Close InputStream and OutpuStream. Release the lock.
	 * 
	 * @throws IOException
	 */
	public void release() throws IOException {
		synchronized (file) {
			IOUtils.close(fileInputStream, fileOutputStream);
			if (fileLock != null)
				fileLock.release();
			fileLock = null;
		}
	}

	public boolean exists() {
		return file.exists();
	}

	public long length() {
		return file.length();
	}

	public long lastModified() {
		return file.lastModified();
	}

}
