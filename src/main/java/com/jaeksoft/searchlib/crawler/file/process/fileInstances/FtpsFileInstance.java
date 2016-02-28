/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;

public class FtpsFileInstance extends FtpFileInstance {

	public FtpsFileInstance() {
		super();
	}

	protected FtpsFileInstance(FilePathItem filePathItem, FtpFileInstance parent, FTPFile ftpFile)
			throws URISyntaxException, UnsupportedEncodingException {
		super(filePathItem, parent, ftpFile);
	}

	@Override
	protected FtpFileInstance newInstance(FilePathItem filePathItem, FtpFileInstance parent, FTPFile ftpFile)
			throws URISyntaxException, UnsupportedEncodingException {
		return new FtpsFileInstance(filePathItem, parent, ftpFile);
	}

	@Override
	public URI init() throws URISyntaxException {
		return new URI("ftps", filePathItem.getHost(), getPath(), null);
	}

	@Override
	protected FTPClient ftpConnect() throws IOException {
		FilePathItem fpi = getFilePathItem();
		FTPClient f = new FTPSClient();
		f.connect(fpi.getHost());
		f.login(fpi.getUsername(), fpi.getPassword());
		return f;
	}

}
