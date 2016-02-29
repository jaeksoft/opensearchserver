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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;

public class FtpFileInstance extends FileInstanceAbstract implements FileInstanceAbstract.SecurityInterface {

	private FTPFile ftpFile;

	public FtpFileInstance() {
		ftpFile = null;
	}

	protected FtpFileInstance(FilePathItem filePathItem, FtpFileInstance parent, FTPFile ftpFile)
			throws URISyntaxException, UnsupportedEncodingException {
		init(filePathItem, parent, LinkUtils.concatPath(parent.getPath(), ftpFile.getName()));
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

	protected FTPClient ftpConnect() throws SocketException, IOException {
		FilePathItem fpi = getFilePathItem();
		FTPClient ftp = null;
		try {
			ftp = new FTPClient();
			// For debug
			// f.addProtocolCommandListener(new PrintCommandListener(
			// new PrintWriter(System.out)));
			ftp.setConnectTimeout(120000);
			ftp.setControlKeepAliveTimeout(180);
			ftp.setDataTimeout(120000);
			ftp.connect(fpi.getHost());
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new IOException("FTP Error Code: " + reply);
			ftp.login(fpi.getUsername(), fpi.getPassword());
			if (!FTPReply.isPositiveCompletion(reply))
				throw new IOException("FTP Error Code: " + reply);
			if (fpi.isFtpUsePassiveMode())
				ftp.enterLocalPassiveMode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new IOException("FTP Error Code: " + reply);
			FTPClient ftpReturn = ftp;
			ftp = null;
			return ftpReturn;
		} finally {
			if (ftp != null)
				ftpQuietDisconnect(ftp);
		}
	}

	private void ftpQuietDisconnect(FTPClient f) {
		if (f == null)
			return;
		try {
			if (f.isConnected())
				f.disconnect();
		} catch (IOException e) {
			Logging.warn(e);
		}
	}

	protected FtpFileInstance newInstance(FilePathItem filePathItem, FtpFileInstance parent, FTPFile ftpFile)
			throws URISyntaxException, UnsupportedEncodingException {
		return new FtpFileInstance(filePathItem, parent, ftpFile);
	}

	private FileInstanceAbstract[] buildFileInstanceArray(FTPFile[] files)
			throws URISyntaxException, UnsupportedEncodingException {
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

		public FtpInstanceFileFilter(boolean ignoreHiddenFiles, boolean fileOnly, Matcher[] exclusionMatchers) {
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
				if (RegExpUtils.matches(ff.getLink(), exclusionMatchers))
					return false;
			return true;
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws IOException, URISyntaxException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(getPath(), new FtpInstanceFileFilter(filePathItem.isIgnoreHiddenFiles(), true,
					filePathItem.getExclusionMatchers()));
			return buildFileInstanceArray(files);
		} finally {
			ftpQuietDisconnect(f);
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories() throws IOException, URISyntaxException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			FTPFile[] files = f.listFiles(getPath(), new FtpInstanceFileFilter(filePathItem.isIgnoreHiddenFiles(),
					false, filePathItem.getExclusionMatchers()));
			return buildFileInstanceArray(files);
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
	public String getFileName() {
		if (ftpFile == null)
			return null;
		return ftpFile.getName();
	}

	@Override
	public void delete() throws IOException {
		throw new IOException("Delete not implemented");
	}

	@Override
	public InputStream getInputStream() throws IOException {
		FTPClient f = null;
		try {
			f = ftpConnect();
			f.setFileType(FTP.BINARY_FILE_TYPE);
			return f.retrieveFileStream(getPath());
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

		if (ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) {
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
