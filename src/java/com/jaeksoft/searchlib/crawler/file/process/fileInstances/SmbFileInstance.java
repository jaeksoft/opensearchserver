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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SmbFileInstance extends FileInstanceAbstract {

	private SmbFile smbFileStore;

	public SmbFileInstance() {
		smbFileStore = null;
	}

	private SmbFileInstance(FilePathItem filePathItem, SmbFileInstance parent,
			SmbFile smbFile) throws URISyntaxException, SearchLibException {
		init(filePathItem, parent,
				LinkUtils.concatPath(parent.getPath(), smbFile.getName()));
		this.smbFileStore = smbFile;
	}

	@Override
	public URI init() throws URISyntaxException {
		return new URI("smb", filePathItem.getHost(), getPath(), null);
	}

	private SmbFile getSmbFile() throws MalformedURLException {
		if (smbFileStore != null)
			return smbFileStore;
		URL url = getURI().toURL();
		if (filePathItem.isGuest())
			smbFileStore = new SmbFile(url);
		else {
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
					filePathItem.getDomain(), filePathItem.getUsername(),
					filePathItem.getPassword());
			smbFileStore = new SmbFile(url, auth);
		}
		return smbFileStore;
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			if (smbFile.isDirectory())
				return FileTypeEnum.directory;
			if (smbFile.isFile())
				return FileTypeEnum.file;
			return null;
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (SmbException e) {
			throw new SearchLibException(e);
		}
	}

	private FileInstanceAbstract[] buildFileInstanceArray(SmbFile[] files)
			throws URISyntaxException, SearchLibException {
		if (files == null)
			return null;
		FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[files.length];
		int i = 0;
		for (SmbFile file : files)
			fileInstances[i++] = new SmbFileInstance(filePathItem, this, file);
		return fileInstances;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile.listFiles();
			return buildFileInstanceArray(files);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (SmbException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	private class SmbFileOnlyFilter implements SmbFileFilter {

		@Override
		public boolean accept(SmbFile file) throws SmbException {
			return file.isFile();
		}

	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile.listFiles(new SmbFileOnlyFilter());
			return buildFileInstanceArray(files);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (SmbException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public Long getLastModified() throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			return smbFile.getLastModified();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public Long getFileSize() throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			return (long) smbFile.getContentLength();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		SmbFile smbFile = getSmbFile();
		return smbFile.getInputStream();
	}

}
