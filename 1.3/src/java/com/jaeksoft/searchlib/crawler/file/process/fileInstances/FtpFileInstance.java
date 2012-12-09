/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.util.LinkUtils;

public class FtpFileInstance extends FileInstanceAbstract {

	private FTPFile ftpFile;

	public FtpFileInstance() {
		ftpFile = null;
	}

	protected FtpFileInstance(FilePathItem filePathItem,
			FtpFileInstance parent, FTPFile ftpFile) throws URISyntaxException,
			SearchLibException {
		init(filePathItem, parent,
				LinkUtils.concatPath(parent.getPath(), ftpFile.getName()));
		this.ftpFile = ftpFile;
	}

	@Override
	public URI init() throws URISyntaxException {
		return new URI("ftp", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() {
		if (ftpFile == null)
			return FileTypeEnum.directory;
		switch (ftpFile.getType()) {
		case FTPFile.DIRECTORY_TYPE:
			return FileTypeEnum.directory;
		case FTPFile.FILE_TYPE:
			return FileTypeEnum.file;
		}
		return null;
	}

	protected FTPFile getFTPFile() {
		return ftpFile;
	}

	protected FTPClient ftpConnect() throws SocketException, IOException,
			NoSuchAlgorithmException {
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
			Logging.warn(e);
		}
	}

	protected FtpFileInstance newInstance(FilePathItem filePathItem,
			FtpFileInstance parent, FTPFile ftpFile) throws URISyntaxException,
			SearchLibException {
		return new FtpFileInstance(filePathItem, parent, ftpFile);
	}

	private FileInstanceAbstract[] buildFileInstanceArray(FTPFile[] files)
			throws URISyntaxException, SearchLibException {
		if (files == null)
			return null;
		FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[files.length];
		int i = 0;
		for (FTPFile file : files)
			fileInstances[i++] = newInstance(filePathItem, this, file);
		return fileInstances;
	}

	public static class IgnoreHiddenFilter implements FTPFileFilter {
		@Override
		public boolean accept(FTPFile ff) {
			String name = ff.getName();
			if (name == null)
				return false;
			if (name.startsWith("."))
				return false;
			return true;
		}
	}

	public static class FileOnlyDirectoryFilter extends IgnoreHiddenFilter {

		@Override
		public boolean accept(FTPFile ff) {
			if (!super.accept(ff))
				return false;
			return ff.getType() == FTPFile.FILE_TYPE;
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws SearchLibException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(getPath(),
					new FileOnlyDirectoryFilter());
			return buildFileInstanceArray(files);
		} catch (SocketException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			ftpQuietDisconnect(f);
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws SearchLibException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(getPath(), new IgnoreHiddenFilter());
			return buildFileInstanceArray(files);
		} catch (SocketException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			ftpQuietDisconnect(f);
		}
	}

	@Override
	public Long getLastModified() {
		if (ftpFile == null)
			return null;
		return ftpFile.getTimestamp().getTimeInMillis();
	}

	@Override
	public Long getFileSize() {
		if (ftpFile == null)
			return null;
		return ftpFile.getSize();
	}

	@Override
	public String getFileName() throws SearchLibException {
		if (ftpFile == null)
			return null;
		return ftpFile.getName();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			return f.retrieveFileStream(getPath());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		} finally {
			ftpQuietDisconnect(f);
		}
	}

}
