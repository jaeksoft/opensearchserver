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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class DropboxFileInstance extends FileInstanceAbstract {

	private static final DbxRequestConfig DBX_REQUEST_CONFIG = new DbxRequestConfig(
			"opensearcjserver/0.1", "en");

	private static final DbxAppInfo DBX_APP_INFO = new DbxAppInfo(
			"2q1itz9v9manpv9", "hjpihpchb0xwd3d");

	private DbxEntry dbxEntry;

	public DropboxFileInstance() {
		super();
	}

	protected DropboxFileInstance(FilePathItem filePathItem,
			DropboxFileInstance parent, DbxEntry dbxEntry)
			throws URISyntaxException, SearchLibException,
			UnsupportedEncodingException {
		init(filePathItem, parent, dbxEntry.path);
		this.dbxEntry = dbxEntry;
	}

	public static DbxWebAuth requestAuthorization() throws SearchLibException {
		// TODO do the implementation
		throw new SearchLibException("Not yet implemented");
	}

	public static String retrieveAccessToken(DbxWebAuth webAuth,
			StringBuffer sbUid) throws SearchLibException {
		// TODO do the implementation
		throw new SearchLibException("Not yet implemented");
	}

	private DbxClient connect() throws IOException {
		// TODO do the implementation
		throw new IOException("Not yet implemented");
	}

	@Override
	public URI init() throws SearchLibException, URISyntaxException {
		return new URI("dropbox", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		if (dbxEntry == null) // ROOT
			return FileTypeEnum.directory;
		if (dbxEntry.isFolder())
			return FileTypeEnum.directory;
		return FileTypeEnum.file;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		try {
			DbxClient dbxClient = connect();
			DbxEntry.WithChildren entries;
			entries = dbxClient.getMetadataWithChildren(getPath());
			if (entries == null || entries.children == null)
				return null;
			FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[entries.children
					.size()];
			int i = 0;
			for (DbxEntry entry : entries.children)
				fileInstances[i++] = new DropboxFileInstance(filePathItem,
						this, entry);
			return fileInstances;
		} catch (DbxException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public FileInstanceAbstract[] listFilesOnly() throws URISyntaxException,
			SearchLibException, UnsupportedEncodingException {
		try {
			DbxClient dbxClient = connect();
			DbxEntry.WithChildren entries;
			entries = dbxClient.getMetadataWithChildren(getPath());
			if (entries == null || entries.children == null)
				return null;
			int l = 0;
			for (DbxEntry entry : entries.children)
				if (entry.isFile())
					l++;
			FileInstanceAbstract[] fileInstances = new FileInstanceAbstract[l];
			int i = 0;
			for (DbxEntry entry : entries.children)
				if (entry.isFile())
					fileInstances[i++] = new DropboxFileInstance(filePathItem,
							this, entry);
			return fileInstances;
		} catch (DbxException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public Long getLastModified() throws SearchLibException {
		if (dbxEntry == null)
			return null;
		Date dt = null;
		if (dbxEntry instanceof DbxEntry.File)
			dt = ((DbxEntry.File) dbxEntry).lastModified;
		if (dt == null)
			return null;
		return dt.getTime();
	}

	@Override
	public Long getFileSize() throws SearchLibException {
		if (dbxEntry == null)
			return null;
		if (dbxEntry instanceof DbxEntry.File)
			return ((DbxEntry.File) dbxEntry).numBytes;
		return null;
	}

	@Override
	public String getFileName() throws SearchLibException {
		if (dbxEntry == null)
			return null;
		return dbxEntry.name;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			DbxClient dbxClient = connect();
			DbxClient.Downloader downloader;
			downloader = dbxClient.startGetFile(getPath(), null);
			return downloader.body;
		} catch (DbxException e) {
			throw new IOException(e);
		}
	}

}
