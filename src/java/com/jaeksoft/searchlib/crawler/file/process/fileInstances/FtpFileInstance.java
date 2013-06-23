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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;

public class FtpFileInstance extends FileInstanceAbstract implements
		FileInstanceAbstract.SecurityInterface {

	private FTPFile ftpFile;

	public FtpFileInstance() {
		ftpFile = null;
	}

	protected FtpFileInstance(FilePathItem filePathItem,
			FtpFileInstance parent, FTPFile ftpFile) throws URISyntaxException,
			SearchLibException, UnsupportedEncodingException {
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
		f.setControlKeepAliveTimeout(180);
		f.setConnectTimeout(120000);
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
			SearchLibException, UnsupportedEncodingException {
		return new FtpFileInstance(filePathItem, parent, ftpFile);
	}

	private FileInstanceAbstract[] buildFileInstanceArray(FTPFile[] files)
			throws URISyntaxException, SearchLibException,
			UnsupportedEncodingException {
		if (files == null)
			return null;
		FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[files.length];
		int i = 0;
		for (FTPFile file : files)
			fileInstances[i++] = newInstance(filePathItem, this, file);
		return fileInstances;
	}

	public static class FtpInstanceFileFilter implements FTPFileFilter {

		private final Matcher[] exclusionMatchers;
		private final boolean ignoreHiddenFiles;
		private final boolean fileOnly;

		public FtpInstanceFileFilter(boolean ignoreHiddenFiles,
				boolean fileOnly, Matcher[] exclusionMatchers) {
			this.exclusionMatchers = exclusionMatchers;
			this.ignoreHiddenFiles = ignoreHiddenFiles;
			this.fileOnly = fileOnly;
		}

		@Override
		public boolean accept(FTPFile ff) {
			String name = ff.getName();
			if (name == null)
				return false;
			if (ignoreHiddenFiles)
				if (name.startsWith("."))
					return false;
			if (fileOnly)
				if (ff.getType() != FTPFile.FILE_TYPE)
					return false;
			if (exclusionMatchers != null)
				if (RegExpUtils.find(ff.getLink(), exclusionMatchers))
					return false;
			return true;
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws SearchLibException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(
					getPath(),
					new FtpInstanceFileFilter(filePathItem
							.isIgnoreHiddenFiles(), true, filePathItem
							.getExclusionMatchers()));
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
			FTPFile[] files = f.listFiles(
					getPath(),
					new FtpInstanceFileFilter(filePathItem
							.isIgnoreHiddenFiles(), false, filePathItem
							.getExclusionMatchers()));
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

	public static List<SecurityAccess> getSecurity(FTPFile ftpFile) {
		List<SecurityAccess> accesses = new ArrayList<SecurityAccess>();
		if (ftpFile == null)
			return accesses;
		if (ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
			SecurityAccess access = new SecurityAccess();
			access.setGrant(SecurityAccess.Grant.ALLOW);
			access.setType(SecurityAccess.Type.USER);
			access.setId(ftpFile.getUser());
			accesses.add(access);
		}

		if (ftpFile
				.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) {
			SecurityAccess access = new SecurityAccess();
			access.setGrant(SecurityAccess.Grant.ALLOW);
			access.setType(SecurityAccess.Type.GROUP);
			access.setId(ftpFile.getGroup());
			accesses.add(access);
		}

		return accesses;
	}

	@Override
	public List<SecurityAccess> getSecurity() throws IOException {
		return getSecurity(getFTPFile());
	}
}
