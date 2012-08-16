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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class DropboxFileInstance extends FileInstanceAbstract {

	private static final AppKeyPair APPKEYPAIR = new AppKeyPair(
			"2q1itz9v9manpv9", "hjpihpchb0xwd3d");

	private static final Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

	private DropboxAPI.Entry dpEntry;

	public DropboxFileInstance() {
		super();
	}

	protected DropboxFileInstance(FilePathItem filePathItem,
			DropboxFileInstance parent, DropboxAPI.Entry dpEntry)
			throws URISyntaxException, SearchLibException {
		init(filePathItem, parent, dpEntry.path);
		this.dpEntry = dpEntry;
	}

	public static WebAuthInfo requestAuthorization() throws SearchLibException {
		try {
			WebAuthSession session = new WebAuthSession(APPKEYPAIR, ACCESS_TYPE);
			return session.getAuthInfo();
		} catch (DropboxException e) {
			throw new SearchLibException(e);
		}
	}

	public static AccessTokenPair retrieveAccessToken(WebAuthInfo webAuthInfo,
			StringBuffer sbUid) throws SearchLibException {
		try {
			WebAuthSession session = new WebAuthSession(APPKEYPAIR, ACCESS_TYPE);
			sbUid.append(session
					.retrieveWebAccessToken(webAuthInfo.requestTokenPair));
			return session.getAccessTokenPair();
		} catch (DropboxException e) {
			throw new SearchLibException(e);
		}
	}

	private DropboxAPI<WebAuthSession> connect() {
		WebAuthSession session = new WebAuthSession(APPKEYPAIR, ACCESS_TYPE,
				new AccessTokenPair(filePathItem.getUsername(),
						filePathItem.getPassword()));
		return new DropboxAPI<WebAuthSession>(session);
	}

	@Override
	public URI init() throws SearchLibException, URISyntaxException {
		return new URI("dropbox", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		if (dpEntry == null) // ROOT
			return FileTypeEnum.directory;
		if (dpEntry.isDir)
			return FileTypeEnum.directory;
		return FileTypeEnum.file;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		try {
			DropboxAPI<?> dbAPI = connect();
			DropboxAPI.Entry entries = dbAPI.metadata(getPath(), 0, null, true,
					null);
			if (entries == null || entries.contents == null)
				return null;
			int l = 0;
			for (DropboxAPI.Entry entry : entries.contents)
				if (!entry.isDeleted)
					l++;
			FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[l];
			int i = 0;
			for (DropboxAPI.Entry entry : entries.contents)
				if (!entry.isDeleted)
					fileInstances[i++] = new DropboxFileInstance(filePathItem,
							this, entry);
			return fileInstances;
		} catch (DropboxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException {
		try {
			DropboxAPI<WebAuthSession> dbAPI = connect();
			DropboxAPI.Entry entries = dbAPI.metadata(getPath(), 0, null, true,
					null);
			if (entries == null || entries.contents == null)
				return null;
			int l = 0;
			for (DropboxAPI.Entry entry : entries.contents)
				if (!entry.isDir && !entry.isDeleted)
					l++;
			FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[l];
			int i = 0;
			for (DropboxAPI.Entry entry : entries.contents)
				if (!entry.isDir && !entry.isDeleted)
					fileInstances[i++] = new DropboxFileInstance(filePathItem,
							this, entry);
			return fileInstances;
		} catch (DropboxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public Long getLastModified() throws SearchLibException {
		if (dpEntry == null)
			return null;
		return RESTUtility.parseDate(dpEntry.modified).getTime();
	}

	@Override
	public Long getFileSize() throws SearchLibException {
		if (dpEntry == null)
			return null;
		return dpEntry.bytes;
	}

	@Override
	public String getFileName() throws SearchLibException {
		if (dpEntry == null)
			return null;
		return dpEntry.fileName();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			DropboxAPI<WebAuthSession> dbAPI = connect();
			return dbAPI.getFileStream(getPath(), null);
		} catch (DropboxException e) {
			throw new IOException(e);
		}
	}

}
