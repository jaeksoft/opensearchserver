/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.exec.OS;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.json.JSONException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class IndexDirectory {

	private Directory directory;
	private final ReadWriteLock rwl = new ReadWriteLock();

	protected IndexDirectory(File indexDir) throws IOException {
		if (OS.isFamilyWindows())
			directory = FSDirectory.open(indexDir);
		else
			directory = NIOFSDirectory.open(indexDir);
	}

	/**
	 * Create an index directory using an index remotely stored using the Object
	 * Storage API.
	 * 
	 * The URI should be created like that:
	 * SWIFT://localhost?tenant=&container=&user=&password=&url=
	 * 
	 * The parameters must be URL encoded as UTF-8
	 * 
	 * @param uri
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws JSONException
	 * @throws SearchLibException
	 */
	protected IndexDirectory(final URI uri) throws IOException,
			URISyntaxException, JSONException, SearchLibException {
		if ("SWIFT".equals(uri.getScheme())) {
			HttpDownloader httpDownloader = new HttpDownloader(null, true, null);
			Map<String, String> parameters = LinkUtils
					.getUniqueQueryParameters(uri, "UTF-8");
			String tenant = parameters.get("tenant");
			String container = parameters.get("container");
			String user = parameters.get("user");
			String password = parameters.get("password");
			String url = parameters.get("url");
			SwiftToken token = new SwiftToken(httpDownloader, url, user,
					password, AuthType.KEYSTONE, tenant);
			directory = new ObjectStorageDirectory(httpDownloader, token,
					container);
			return;
		}
		throw new IOException("Unsupported protocol: " + uri);
	}

	public Directory getDirectory() {
		rwl.r.lock();
		try {
			return directory;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isEmpty() throws IOException {
		rwl.r.lock();
		try {
			return ArrayUtils.isEmpty(directory.listAll());
		} catch (NoSuchDirectoryException e) {
			return true;
		} finally {
			rwl.r.unlock();
		}
	}

	public void unlock() {
		rwl.w.lock();
		try {
			if (directory == null)
				return;
			if (!IndexWriter.isLocked(directory))
				return;
			IndexWriter.unlock(directory);
		} catch (Throwable e) {
			Logging.warn(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void close() {
		rwl.w.lock();
		try {
			if (directory == null)
				return;
			try {
				directory.close();
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
			}
			directory = null;
		} finally {
			rwl.w.unlock();
		}
	}

}
