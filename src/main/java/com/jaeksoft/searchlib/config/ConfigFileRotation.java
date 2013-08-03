/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.util.SimpleLock;

public class ConfigFileRotation {

	private File masterFile;
	private File tempFile;
	private PrintWriter tempPrintWriter;
	private File oldFile;

	private final SimpleLock lock = new SimpleLock();

	protected ConfigFileRotation(File directory, String masterName) {
		String ext = FilenameUtils.getExtension(masterName);
		String baseName = FilenameUtils.getBaseName(masterName);
		init(directory, masterName, baseName + "_tmp." + ext, baseName
				+ "_old." + ext);
	}

	private void init(File directory, String masterName, String tempName,
			String oldName) {
		lock.rl.lock();
		try {
			this.masterFile = new File(directory, masterName);
			this.tempFile = new File(directory, tempName);
			this.oldFile = new File(directory, oldName);
			this.tempPrintWriter = null;
		} finally {
			lock.rl.unlock();
		}
	}

	private void freeTempPrintWriter() {
		if (tempPrintWriter == null)
			return;
		tempPrintWriter.close();
		tempPrintWriter = null;
	}

	public void abort() {
		lock.rl.lock();
		try {
			freeTempPrintWriter();
		} finally {
			lock.rl.unlock();
		}
	}

	public void rotate() throws IOException {
		lock.rl.lock();
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
			lock.rl.unlock();
		}
	}

	public void delete() throws IOException {
		lock.rl.lock();
		try {
			freeTempPrintWriter();
			if (oldFile.exists())
				oldFile.delete();
			if (masterFile.exists())
				FileUtils.moveFile(masterFile, oldFile);
		} finally {
			lock.rl.unlock();
		}
	}

	public PrintWriter getTempPrintWriter(String encoding) throws IOException {
		lock.rl.lock();
		try {
			if (tempPrintWriter != null)
				return tempPrintWriter;
			if (!tempFile.exists())
				FileUtils.touch(tempFile);
			tempPrintWriter = new PrintWriter(tempFile, encoding);
			return tempPrintWriter;
		} finally {
			lock.rl.unlock();
		}
	}

}
