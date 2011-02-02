/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class ConfigFileRotation {

	private File masterFile;
	private File tempFile;
	private PrintWriter tempPrintWriter;
	private File oldFile;

	private final Lock lock = new ReentrantLock(true);

	protected ConfigFileRotation(File directory, String masterName) {
		String ext = FilenameUtils.getExtension(masterName);
		String baseName = FilenameUtils.getBaseName(masterName);
		init(directory, masterName, baseName + "_tmp." + ext, baseName
				+ "_old." + ext);
	}

	private void init(File directory, String masterName, String tempName,
			String oldName) {
		lock.lock();
		try {
			this.masterFile = new File(directory, masterName);
			this.tempFile = new File(directory, tempName);
			this.oldFile = new File(directory, oldName);
			this.tempPrintWriter = null;
		} finally {
			lock.unlock();
		}
	}

	private void freeTempPrintWriter() {
		if (tempPrintWriter == null)
			return;
		tempPrintWriter.close();
		tempPrintWriter = null;
	}

	public void abort() {
		lock.lock();
		try {
			freeTempPrintWriter();
		} finally {
			lock.unlock();
		}
	}

	public void rotate() throws IOException {
		lock.lock();
		try {
			freeTempPrintWriter();
			if (oldFile.exists())
				oldFile.delete();
			if (!tempFile.exists())
				return;
			if (masterFile.exists())
				FileUtils.moveFile(masterFile, oldFile);
			FileUtils.moveFile(tempFile, masterFile);
		} finally {
			lock.unlock();
		}
	}

	public void delete() throws IOException {
		lock.lock();
		try {
			freeTempPrintWriter();
			if (oldFile.exists())
				oldFile.delete();
			if (masterFile.exists())
				FileUtils.moveFile(masterFile, oldFile);
		} finally {
			lock.unlock();
		}
	}

	public PrintWriter getTempPrintWriter() throws IOException {
		lock.lock();
		try {
			if (tempPrintWriter != null)
				return tempPrintWriter;
			if (!tempFile.exists())
				FileUtils.touch(tempFile);
			tempPrintWriter = new PrintWriter(tempFile);
			return tempPrintWriter;
		} finally {
			lock.unlock();
		}
	}

}
