/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract.SecurityInterface;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.StringUtils;

import jcifs.smb.ACE;
import jcifs.smb.Kerb5Authenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

public class SmbFileInstance extends FileInstanceAbstract implements SecurityInterface {

	static {
		if (StringUtils.isEmpty(System.getProperty("java.protocol.handler.pkgs")))
			System.setProperty("java.protocol.handler.pkgs", "jcifs");
		if (StringUtils.isEmpty("jicfs.resolveOrder"))
			System.setProperty("jicfs.resolveOrder", "LMHOSTS,DNS,WINS");
		if (StringUtils.isEmpty("jcifs.smb.client.capabilities"))
			System.setProperty("jcifs.smb.client.capabilities", Kerb5Authenticator.CAPABILITIES);
		if (StringUtils.isEmpty("jcifs.smb.client.flags2"))
			System.setProperty("jcifs.smb.client.flags2", Kerb5Authenticator.FLAGS2);
		if (StringUtils.isEmpty("jcifs.smb.client.signingPreferred"))
			System.setProperty("jcifs.smb.client.signingPreferred", "true");
	}

	public static enum SmbSecurityPermissions {

		SHARE_PERMISSIONS("Share permissions"),

		FILE_PERMISSIONS("File permissions"),

		FILE_SHARE_PERMISSIONS("File & share permissions");

		private final String label;

		private SmbSecurityPermissions(String label) {
			this.label = label;
		}

		public static SmbSecurityPermissions find(String type) {
			for (SmbSecurityPermissions securityPermission : values())
				if (securityPermission.name().equalsIgnoreCase(type))
					return securityPermission;
			return null;
		}

		public String getLabel() {
			return label;
		}
	}

	private SmbFile smbFileStore;

	public SmbFileInstance() {
		smbFileStore = null;
	}

	protected SmbFileInstance(FilePathItem filePathItem, SmbFileInstance parent, SmbFile smbFile)
			throws URISyntaxException, SearchLibException, UnsupportedEncodingException {
		init(filePathItem, parent, LinkUtils.concatPath(parent.getPath(), smbFile.getName()));
		this.smbFileStore = smbFile;
	}

	@Override
	public URI init() throws URISyntaxException {
		return new URI("smb", filePathItem.getHost(), getPath(), null);
	}

	protected SmbFile getSmbFile() throws MalformedURLException {
		if (smbFileStore != null)
			return smbFileStore;
		String context = StringUtils.fastConcat("smb://", getFilePathItem().getHost());
		if (filePathItem.isGuest()) {
			smbFileStore = new SmbFile(context, getPath());
		} else {
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(filePathItem.getDomain(),
					filePathItem.getUsername(), filePathItem.getPassword());
			smbFileStore = new SmbFile(context, getPath(), auth);
		}
		if (Logging.isDebug)
			Logging.debug("SMB Connect to (without auth) " + smbFileStore.getURL().toString());
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
			throw new SearchLibException("URL error on " + getPath(), e);
		} catch (SmbException e) {
			throw new SearchLibException("SMB Error on " + getPath(), e);
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
			throw new SearchLibException("URL error on " + getPath(), e);
		}
	}

	protected SmbFileInstance newInstance(FilePathItem filePathItem, SmbFileInstance parent, SmbFile smbFile)
			throws URISyntaxException, SearchLibException, UnsupportedEncodingException {
		return new SmbFileInstance(filePathItem, parent, smbFile);
	}

	private FileInstanceAbstract[] buildFileInstanceArray(SmbFile[] files)
			throws URISyntaxException, SearchLibException, UnsupportedEncodingException {
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
			throws SearchLibException, UnsupportedEncodingException, URISyntaxException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile.listFiles(new SmbInstanceFileFilter(false));
			return buildFileInstanceArray(files);
		} catch (SmbAuthException e) {
			Logging.warn(e.getMessage() + " - " + getPath(), e);
			return null;
		} catch (SmbException e) {
			throw new SearchLibException(e);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	private class SmbInstanceFileFilter implements SmbFileFilter {

		private final Matcher[] exclusionMatcher;
		private final boolean ignoreHiddenFiles;
		private final boolean fileOnly;

		private SmbInstanceFileFilter(boolean fileOnly) {
			this.ignoreHiddenFiles = filePathItem.isIgnoreHiddenFiles();
			this.exclusionMatcher = filePathItem.getExclusionMatchers();
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
			if (exclusionMatcher != null)
				if (RegExpUtils.matches(f.getPath(), exclusionMatcher))
					return false;
			return true;
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly()
			throws SearchLibException, UnsupportedEncodingException, URISyntaxException {
		try {
			SmbFile smbFile = getSmbFile();
			SmbFile[] files = smbFile.listFiles(new SmbInstanceFileFilter(true));
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
	public void delete() throws IOException {
		getSmbFile().delete();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			SmbFile smbFile = getSmbFile();
			return smbFile.getInputStream();
		} catch (IOException e) {
			throw new IOException("I/O error on SMB path: " + getPath(), e);
		}
	}

	public final static ACE[] getSecurity(SmbFile smbFile) throws IOException {
		try {
			return smbFile.getSecurity();
		} catch (SmbAuthException e) {
			Logging.warn(e.getMessage() + " - " + smbFile.getPath(), e);
			return null;
		} catch (SmbException e) {
			if (e.getNtStatus() == 0xC00000BB)
				return null;
			throw e;
		}
	}

	public final static ACE[] getShareSecurity(SmbFile smbFile) throws IOException {
		try {
			return smbFile.getShareSecurity(false);
		} catch (SmbAuthException e) {
			Logging.warn(e.getMessage() + " - " + smbFile.getPath(), e);
			return null;
		} catch (SmbException e) {
			if (e.getNtStatus() == 0xC00000BB)
				return null;
			throw e;
		}
	}

	private void fillSecurity(ACE[] aces, List<SecurityAccess> accesses) {
		if (aces == null)
			return;
		for (ACE ace : aces) {
			if ((ace.getAccessMask() & ACE.FILE_READ_DATA) == 0)
				continue;
			SID sid = ace.getSID();
			SecurityAccess accessName = new SecurityAccess();
			SecurityAccess accessSid = new SecurityAccess();
			accessName.setId(sid.toDisplayString().toLowerCase());
			accessSid.setId(sid.toString());
			if (ace.isAllow()) {
				accessName.setGrant(SecurityAccess.Grant.ALLOW);
				accessSid.setGrant(SecurityAccess.Grant.ALLOW);
			} else {
				accessName.setGrant(SecurityAccess.Grant.DENY);
				accessSid.setGrant(SecurityAccess.Grant.DENY);
			}
			switch (sid.getType()) {
			case SID.SID_TYPE_USER:
				accessName.setType(SecurityAccess.Type.USER);
				accessSid.setType(SecurityAccess.Type.USER);
				break;
			case SID.SID_TYPE_DOM_GRP:
			case SID.SID_TYPE_DOMAIN:
			case SID.SID_TYPE_ALIAS:
			case SID.SID_TYPE_WKN_GRP:
				accessName.setType(SecurityAccess.Type.GROUP);
				accessSid.setType(SecurityAccess.Type.GROUP);
				break;
			case SID.SID_TYPE_DELETED:
			case SID.SID_TYPE_INVALID:
			case SID.SID_TYPE_UNKNOWN:
			case SID.SID_TYPE_USE_NONE:
				break;
			}
			accesses.add(accessName);
			accesses.add(accessSid);
		}
	}

	@Override
	public List<SecurityAccess> getSecurity() throws IOException {
		SmbFile smbFile = getSmbFile();
		List<SecurityAccess> accesses = new ArrayList<SecurityAccess>();
		SmbSecurityPermissions smbSecurityPermissions = filePathItem.getSmbSecurityPermissions();
		if (smbSecurityPermissions == null)
			smbSecurityPermissions = SmbSecurityPermissions.FILE_PERMISSIONS;
		switch (smbSecurityPermissions) {
		case FILE_PERMISSIONS:
			fillSecurity(getSecurity(smbFile), accesses);
			break;
		case SHARE_PERMISSIONS:
			fillSecurity(getShareSecurity(smbFile), accesses);
			break;
		case FILE_SHARE_PERMISSIONS:
			fillSecurity(getSecurity(smbFile), accesses);
			fillSecurity(getShareSecurity(smbFile), accesses);
			break;
		}
		return accesses.isEmpty() ? null : accesses;
	}
}
