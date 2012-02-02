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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class HadoopCrawlCache extends CrawlCacheProvider {

	private final static String PATH_HTTP_DOWNLOAD_CACHE = Path.SEPARATOR
			+ "opensearchserver" + Path.SEPARATOR + "http-download-cache";

	private final static String META_EXTENSION = "meta";

	private final static String CONTENT_EXTENSION = "content";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private FileSystem fileSystem;

	private Configuration configuration;

	public HadoopCrawlCache() {
		configuration = null;
		fileSystem = null;
	}

	private String[] configFiles = { "core-default.xml", "core-site.xml" };

	@Override
	public void init(String configString) throws IOException {
		rwl.w.lock();
		try {
			if (fileSystem != null)
				IOUtils.closeQuietly(fileSystem);
			configuration = new Configuration();
			for (String configFile : configFiles)
				configuration.addResource(new Path(configString, configFile));
			fileSystem = FileSystem.get(configuration);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (fileSystem != null)
				IOUtils.closeQuietly(fileSystem);
			fileSystem = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public String getInfos() throws IOException {
		rwl.r.lock();
		try {
			if (configuration == null)
				return null;
			return configuration.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	private Path uriToPath(URI uri, String extension) {
		String path = super.uriToPath(uri, PATH_HTTP_DOWNLOAD_CACHE, 10,
				Path.SEPARATOR, extension, 32);
		return new Path(path);
	}

	@Override
	public InputStream store(DownloadItem downloadItem) throws IOException,
			JSONException {
		rwl.r.lock();
		try {
			URI uri = downloadItem.getUri();

			Path path = checkPath(uriToPath(uri, META_EXTENSION));
			write(path, downloadItem.getMetaAsJson());
			path = checkPath(uriToPath(uri, CONTENT_EXTENSION));
			InputStream is = downloadItem.getContentInputStream();
			write(path, is);
			IOUtils.closeQuietly(is);
			return fileSystem.open(path);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public DownloadItem load(URI uri, long expirationTime) throws IOException,
			JSONException, URISyntaxException {
		rwl.r.lock();
		try {
			Path path = uriToPath(uri, META_EXTENSION);
			if (!fileSystem.exists(path))
				return null;
			if (expirationTime != 0)
				if (fileSystem.getFileStatus(path).getModificationTime() < expirationTime)
					return null;
			String content = read(path);
			JSONObject json = new JSONObject(content);
			DownloadItem downloadItem = new DownloadItem(uri);
			downloadItem.loadMetaFromJson(json);
			path = uriToPath(uri, CONTENT_EXTENSION);
			downloadItem.setContentInputStream(fileSystem.open(path));
			return downloadItem;
		} finally {
			rwl.r.unlock();
		}
	}

	private final long purge(FileStatus[] files, long expiration)
			throws IOException {
		long count = 0;
		for (FileStatus file : files) {
			if (file.isDir()) {
				Path p = file.getPath();
				count += purge(fileSystem.listStatus(p), expiration);
				FileStatus[] fs = fileSystem.listStatus(p);
				if (fs.length == 0)
					if (fileSystem.delete(p, false))
						count++;
			} else {
				if (file.getModificationTime() < expiration)
					if (fileSystem.delete(file.getPath(), false))
						count++;
			}
		}
		return count;
	}

	@Override
	public long flush(long expiration) throws IOException {
		rwl.r.lock();
		try {
			Path path = new Path(PATH_HTTP_DOWNLOAD_CACHE);
			return purge(fileSystem.listStatus(path), expiration);
		} finally {
			rwl.r.unlock();
		}
	}

	private String read(Path path) throws IOException {
		FSDataInputStream in = fileSystem.open(path);
		try {
			return in.readUTF();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private Path checkPath(Path path) throws IOException {
		if (!fileSystem.exists(path)) {
			Path parent = path.getParent();
			if (!fileSystem.exists(parent))
				fileSystem.mkdirs(parent);
		}
		return path;
	}

	private void write(Path path, String content) throws IOException {
		FSDataOutputStream out = fileSystem.create(path, true);
		try {
			out.writeUTF(content);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private void write(Path path, InputStream in) throws IOException {
		FSDataOutputStream out = fileSystem.create(path, true);
		try {
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	public String getConfigurationInformation() {
		return "Please provide the path to the Hadoop configuration (etc) folder";
	}

}
