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
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;

public class DropboxFileInstance extends FileInstanceAbstract {

	private static final AppKeyPair appKeyPair = new AppKeyPair(
			"av7bteaqrirafxs", "c6qem9u91tyuwvg");

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

	private DropboxAPI<WebAuthSession> connect() throws SearchLibException {
		try {
			Scheme http = new Scheme("http", 80,
					PlainSocketFactory.getSocketFactory());

			SSLSocketFactory sf = new SSLSocketFactory(
					SSLContext.getInstance("TLS"),
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			Scheme https = new Scheme("https", 443, sf);
			SchemeRegistry sr = new SchemeRegistry();
			sr.register(http);
			sr.register(https);

			WebAuthSession session = new WebAuthSession(appKeyPair,
					Session.AccessType.DROPBOX);

			System.out.println("DROPBOX URL=" + session.getAuthInfo().url);
			return new DropboxAPI<WebAuthSession>(session);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (DropboxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public URI init() throws SearchLibException, URISyntaxException {
		return new URI("dropbox", filePathItem.getHost(), getPath(), null);
	}

	@Override
	public FileTypeEnum getFileType() throws SearchLibException {
		if (dpEntry == null)
			return FileTypeEnum.directory;
		if (dpEntry.isDir)
			return FileTypeEnum.file;
		return FileTypeEnum.directory;
	}

	@Override
	public FileInstanceAbstract[] listFilesAndDirectories()
			throws URISyntaxException, SearchLibException {
		try {
			DropboxAPI<WebAuthSession> dbAPI = connect();
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
	public InputStream getInputStream() throws IOException {
		try {
			DropboxAPI<WebAuthSession> dbAPI = connect();
			return dbAPI.getFileStream(getPath(), null);
		} catch (SearchLibException e) {
			throw new IOException(e);
		} catch (DropboxException e) {
			throw new IOException(e);
		}
	}

}
