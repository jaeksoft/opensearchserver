/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.crawler.file.process.fileInstances.DropboxFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.FtpsFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.LocalFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SmbFileInstance;
import com.jaeksoft.searchlib.util.ExtensibleEnum;

public class FileInstanceTypeEnum extends ExtensibleEnum<FileInstanceType> {

	public FileInstanceTypeEnum() {
		new FileInstanceType(this, "LocalFileInstance", "Local files", "file",
				LocalFileInstance.class);

		new FileInstanceType(this, "SmbFileInstance", "SMB/CIFS", "smb",
				SmbFileInstance.class);

		new FileInstanceType(this, "FtpFileInstance", "FTP", "ftp",
				FtpFileInstance.class);

		new FileInstanceType(this, "FtpsFileInstance", "FTP over SSL", "ftps",
				FtpsFileInstance.class);

		new FileInstanceType(this, "Dropbox", "Dropbox", "dropbox",
				DropboxFileInstance.class);

	}
}
