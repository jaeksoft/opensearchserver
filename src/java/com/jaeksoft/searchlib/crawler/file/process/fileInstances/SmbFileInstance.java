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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.ACE;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract.SecurityInterface;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SmbFileInstance extends FileInstanceAbstract implements
		SecurityInterface {

	static {
		System.setProperty("java.protocol.handler.pkgs", "jcifs");
		System.setProperty("jicfs.resolveOrder", "LMHOSTS,DNS,WINS");
	}

	private SmbFile smbFileStore;

	public SmbFileInstance() {
		smbFileStore = null;
	}

	protected SmbFileInstance(FilePathItem filePathItem,
			SmbFileInstance parent, SmbFile smbFile) throws URISyntaxException,
			SearchLibException, UnsupportedEncodingException {
		init(filePathItem, parent,
				LinkUtils.concatPath(parent.getPath(), smbFile.getName()));
		this.smbFileStore = smbFile;
	}

	@Override
	public URI init() throws URISyntaxException {
		return new URI("smb", filePathItem.getHost(), getPath(), null);
	}

	protected SmbFile getSmbFile() throws MalformedURLException {
		if (smbFileStore != null)
			return smbFileStore;
		URL url = getURI().toURL();
		if (filePathItem.isGuest()) {
			if (Logging.isDebug)
				Logging.debug("SMB Connect to (without auth) " + url);
			smbFileStore = new SmbFile(url);
		} else {
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
					filePathItem.getDomain(), filePathItem.getUsername(),
					filePathItem.getPassword());
			if (Logging.isDebug)
				Logging.debug("SMB Connect to (with auth) " + url);
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

	@Override
	public String getFileName() throws SearchLibException {
		try {
			SmbFile smbFile = getSmbFile();
			if (smbFile == null)
				return null;
			return smbFile.getName();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	protected SmbFileInstance newInstance(FilePathItem filePathItem,
			SmbFileInstance parent, SmbFile smbFile) throws URISyntaxException,
			SearchLibException, UnsupportedEncodingException {
		return new SmbFileInstance(filePathItem, parent, smbFile);
	}

	private FileInstanceAbstract[] buildFileInstanceArray(SmbFile[] files)
			throws URISyntaxException, SearchLibException,
			UnsupportedEncodingException {
		if (files == null)
			return null;
		FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[files.length];
		int i = 0;
		for (SmbFile file : files)
			fileInstances[i++] = newInstance(filePathItem, this, file);
		return fileInstances;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws SearchLibException, UnsupportedEncodingException,
			URISyntaxException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile
					.listFiles(new SmbInstanceFileFilter(false));
			return buildFileInstanceArray(files);
		} catch (SmbException e) {
			throw new SearchLibException(e);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	private class SmbInstanceFileFilter implements SmbFileFilter {

		private final boolean ignoreHiddenFiles;
		private final boolean fileOnly;

		private SmbInstanceFileFilter(boolean fileOnly) {
			this.ignoreHiddenFiles = filePathItem.isIgnoreHiddenFiles();
			this.fileOnly = fileOnly;
		}

		@Override
		public boolean accept(SmbFile f) throws SmbException {
			if (fileOnly)
				if (!f.isFile())
					return false;
			if (ignoreHiddenFiles)
				if (f.isHidden())
					return false;
			return true;
		}

	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws SearchLibException,
			UnsupportedEncodingException, URISyntaxException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile
					.listFiles(new SmbInstanceFileFilter(true));
			return buildFileInstanceArray(files);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (SmbException e) {
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

	public final static ACE[] getSecurity(SmbFile smbFile) throws IOException {
		try {
			return smbFile.getSecurity();
		} catch (SmbException e) {
			if (e.getNtStatus() == 0xC00000BB)
				return null;
			throw e;
		}
	}

	@Override
	public List<SecurityAccess> getSecurity() throws IOException {
		SmbFile smbFile = getSmbFile();
		ACE[] aces = getSecurity(smbFile);
		if (aces == null)
			return null;
		List<SecurityAccess> accesses = new ArrayList<SecurityAccess>();
		for (ACE ace : aces) {
			if ((ace.getAccessMask() & ACE.FILE_READ_DATA) == 0)
				continue;
			SID sid = ace.getSID();
			SecurityAccess access = new SecurityAccess();
			access.setId(sid.toDisplayString());
			if (ace.isAllow())
				access.setGrant(SecurityAccess.Grant.ALLOW);
			else
				access.setGrant(SecurityAccess.Grant.DENY);
			switch (sid.getType()) {
			case SID.SID_TYPE_USER:
				access.setType(SecurityAccess.Type.USER);
				break;
			case SID.SID_TYPE_DOM_GRP:
			case SID.SID_TYPE_DOMAIN:
			case SID.SID_TYPE_ALIAS:
			case SID.SID_TYPE_WKN_GRP:
				access.setType(SecurityAccess.Type.GROUP);
				break;
			case SID.SID_TYPE_DELETED:
			case SID.SID_TYPE_INVALID:
			case SID.SID_TYPE_UNKNOWN:
			case SID.SID_TYPE_USE_NONE:
				break;
			}
			accesses.add(access);
		}
		return accesses;
	}
}
