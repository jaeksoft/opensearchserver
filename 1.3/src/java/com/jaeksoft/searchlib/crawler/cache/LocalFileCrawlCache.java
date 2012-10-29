/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class LocalFileCrawlCache extends CrawlCacheProvider {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String rootPath = null;

	@Override
	public void close() {
		rwl.w.lock();
		try {
			rootPath = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public String getInfos() throws IOException {
		rwl.r.lock();
		try {
			return rootPath;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void init(String configString) throws IOException {
		rwl.w.lock();
		try {
			File f = new File(configString);
			if (!f.exists())
				throw new IOException("The folder " + f.getAbsolutePath()
						+ " does not exists");
			if (!f.isDirectory())
				throw new IOException("The folder " + f.getAbsolutePath()
						+ " does not exists");
			rootPath = f.getAbsolutePath();
		} finally {
			rwl.w.unlock();
		}
	}

	private final static String PATH_HTTP_DOWNLOAD_CACHE = File.separator
			+ "http-download-cache";

	private final static String META_EXTENSION = "meta";

	private final static String CONTENT_EXTENSION = "content";

	private File uriToFile(URI uri, String extension)
			throws UnsupportedEncodingException {
		String path = super.uriToPath(uri, rootPath + File.separator
				+ PATH_HTTP_DOWNLOAD_CACHE, 10, File.separator, extension, 32);
		return new File(path);
	}

	private File checkPath(File file) throws IOException {
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
		}
		return file;
	}

	@Override
	public InputStream store(DownloadItem downloadItem) throws IOException,
			JSONException {
		rwl.r.lock();
		try {
			URI uri = downloadItem.getUri();
			File file = checkPath(uriToFile(uri, META_EXTENSION));
			FileUtils.writeStringToFile(file, downloadItem.getMetaAsJson());
			file = checkPath(uriToFile(uri, CONTENT_EXTENSION));
			InputStream is = downloadItem.getContentInputStream();
			FileUtils.copyInputStreamToFile(is, file);
			IOUtils.closeQuietly(is);
			return new FileInputStream(file);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public DownloadItem load(URI uri, long expirationTime) throws IOException,
			JSONException, URISyntaxException {
		rwl.r.lock();
		try {
			File file = uriToFile(uri, META_EXTENSION);
			if (!file.exists())
				return null;
			if (expirationTime != 0)
				if (file.lastModified() < expirationTime)
					return null;
			String content = FileUtils.readFileToString(file);
			JSONObject json = new JSONObject(content);
			DownloadItem downloadItem = new DownloadItem(uri);
			downloadItem.loadMetaFromJson(json);
			file = uriToFile(uri, CONTENT_EXTENSION);
			downloadItem.setContentInputStream(new FileInputStream(file));
			return downloadItem;
		} finally {
			rwl.r.unlock();
		}
	}

	private final long purge(File[] files, long expiration) throws IOException {
		long count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				count += purge(file.listFiles(), expiration);
				File[] fs = file.listFiles();
				if (fs.length == 0)
					if (file.delete())
						count++;
			} else {
				if (file.lastModified() < expiration)
					if (file.delete())
						count++;
			}
		}
		return count;
	}

	@Override
	public long flush(long expiration) throws IOException {
		rwl.r.lock();
		try {
			File file = new File(rootPath + File.separator
					+ PATH_HTTP_DOWNLOAD_CACHE);
			return purge(file.listFiles(), expiration);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String getConfigurationInformation() {
		return "Please provide the path of the cache directory (Eg.: /var/local/oss_crawl_cache)";
	}
}
