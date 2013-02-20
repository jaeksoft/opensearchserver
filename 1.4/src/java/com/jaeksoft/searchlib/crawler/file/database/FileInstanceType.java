/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.DropboxFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpsFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.LocalFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SmbFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SwiftFileInstance;

public enum FileInstanceType {

	Local("LocalFileInstance", "Local files", "file", LocalFileInstance.class),

	Smb("SmbFileInstance", "SMB/CIFS", "smb", SmbFileInstance.class),

	Ftp("FtpFileInstance", "FTP", "ftp", FtpFileInstance.class),

	Ftps("FtpsFileInstance", "FTP over SSL", "ftps", FtpsFileInstance.class),

	Dropbox("Dropbox", "Dropbox", "dropbox", DropboxFileInstance.class),

	Swift("Swift", "Swift", "swift", SwiftFileInstance.class);

	private final String name;

	private final String label;

	private final String scheme;

	private final Class<? extends FileInstanceAbstract> classInstance;

	private FileInstanceType(String name, String label, String scheme,
			Class<? extends FileInstanceAbstract> classInstance) {
		this.name = name;
		this.label = label;
		this.scheme = scheme;
		this.classInstance = classInstance;
	}

	/**
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the scheme name
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * 
	 * @return a new instance of the FileInstanceAbstract
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public FileInstanceAbstract getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return classInstance.newInstance();
	}

	public boolean is(Class<? extends FileInstanceAbstract> fileInstanceClass) {
		return classInstance == fileInstanceClass;
	}

	public static FileInstanceType findByScheme(String scheme) {
		if (scheme == null)
			return null;
		for (FileInstanceType fileInstance : values())
			if (fileInstance.scheme.equalsIgnoreCase(scheme))
				return fileInstance;
		return null;
	}

	public static FileInstanceType findByName(String name) {
		if (name == null)
			return null;
		for (FileInstanceType fileInstance : values())
			if (fileInstance.name.equalsIgnoreCase(name))
				return fileInstance;
		return null;
	}
}
