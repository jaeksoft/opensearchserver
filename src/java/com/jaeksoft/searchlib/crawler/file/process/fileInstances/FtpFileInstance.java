/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class FtpFileInstance extends FileInstanceAbstract {

	private FTPFile[] ftpFiles;

	public FtpFileInstance() {
		ftpFiles = null;
	}

	private FtpFileInstance(FilePathItem filePathItem, FtpFileInstance parent,
			FTPFile ftpFile) throws URISyntaxException, SearchLibException {
		init(filePathItem, parent, parent.getPath() + '/' + ftpFile.getName());
	}

	@Override
	public void init() throws SearchLibException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(getPath());
		} catch (SocketException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			ftpQuietDisconnect(f);
		}
	}

	@Override
	public URI getURI() {
		return null;
	}

	@Override
	public FileTypeEnum getFileType() {
		// TODO Auto-generated method stub
		return null;
	}

	private FTPClient ftpConnect() throws SocketException, IOException {
		FilePathItem fpi = getFilePathItem();
		FTPClient f = new FTPClient();
		f.connect(fpi.getHost());
		f.login(fpi.getUsername(), fpi.getPassword());
		return f;
	}

	private void ftpQuietDisconnect(FTPClient f) {
		if (f == null)
			return;
		try {
			f.disconnect();
		} catch (IOException e) {
			Logging.logger.warn(e);
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws SearchLibException {
		return null;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws SearchLibException {
		return null;
	}

	@Override
	public Long getLastModified() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getFileSize() {
		// TODO Auto-generated method stub
		return null;
	}

}
