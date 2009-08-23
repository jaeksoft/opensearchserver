/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class ConfigFileRotation {

	private File directory;
	private File masterFile;
	private File tempFile;
	private File oldFile;

	public ConfigFileRotation(File directory, String masterName,
			String tempName, String oldName) {
		init(directory, masterName, tempName, oldName);
	}

	private void init(File directory, String masterName, String tempName,
			String oldName) {
		this.directory = directory;
		this.masterFile = new File(directory, masterName);
		this.tempFile = new File(directory, tempName);
		this.oldFile = new File(directory, oldName);
	}

	public ConfigFileRotation(File directory, String masterName) {
		String ext = FilenameUtils.getExtension(masterName);
		String baseName = FilenameUtils.getBaseName(masterName);
		init(directory, masterName, baseName + "_tmp." + ext, baseName
				+ "_old." + ext);
	}

	public void rotate() throws IOException {
		synchronized (directory) {
			oldFile.delete();
			if (!tempFile.exists())
				return;
			if (masterFile.exists())
				FileUtils.moveFile(masterFile, oldFile);
			FileUtils.moveFile(tempFile, masterFile);
			// Ensure that several call to rotate delete useful files
			oldFile = null;
			masterFile = null;
			tempFile = null;
		}
	}

	public PrintWriter getTempPrintWriter() throws IOException {
		if (!tempFile.exists())
			FileUtils.touch(tempFile);
		return new PrintWriter(tempFile);
	}

	public File getTempFile() {
		return tempFile;
	}

	public File getOldFile() {
		return oldFile;
	}

	public File getMasterFile() {
		return masterFile;
	}

}
