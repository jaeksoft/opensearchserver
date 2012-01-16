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

package com.jaeksoft.searchlib.hadoop;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.web.StartStopListener;

public class HadoopManager implements Closeable {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final static String HADOOP_PROPERTY_FILE = "hadoop.xml";

	private final static String HADOOP_PROPERTY_ENABLED = "enabled";

	private FileSystem fileSystem;

	private Configuration configuration;

	private boolean enabled;

	private File propFile;

	public HadoopManager(File dataDir) throws InvalidPropertiesFormatException,
			IOException {
		configuration = new Configuration();
		fileSystem = FileSystem.get(configuration);
		propFile = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE,
				HADOOP_PROPERTY_FILE);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		enabled = "true".equals(properties.getProperty(HADOOP_PROPERTY_ENABLED,
				"false"));

	}

	private void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(HADOOP_PROPERTY_ENABLED,
				Boolean.toString(enabled));
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			IOUtils.closeQuietly(fileSystem);
		} finally {
			rwl.w.unlock();
		}
	}

	public String getConfiguration() throws IOException {
		rwl.r.lock();
		try {
			if (configuration == null)
				return null;
			return configuration.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public void reloadConfiguration() throws IOException {
		rwl.w.lock();
		try {
			configuration.reloadConfiguration();
			IOUtils.closeQuietly(fileSystem);
			fileSystem = FileSystem.get(configuration);
		} finally {
			rwl.w.unlock();
		}
	}

	private Path exists(String path) throws IOException {
		Path fsPath = new Path(path);
		if (fileSystem.exists(fsPath))
			return fsPath;
		return null;
	}

	private String read(Path path) throws IOException {
		FSDataInputStream in = fileSystem.open(path);
		try {
			return in.readUTF();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private Path checkPath(String path, boolean replace) throws IOException {
		Path fsPath = new Path(path);
		if (fileSystem.exists(fsPath)) {
			if (!replace)
				throw new IOException("Output already exists: " + path);
			fileSystem.delete(fsPath, false);
		} else
			fileSystem.mkdirs(fsPath.getParent());
		return fsPath;
	}

	private void write(Path path, String content) throws IOException {
		FSDataOutputStream out = fileSystem.create(path);
		try {
			out.writeUTF(content);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private void write(Path path, InputStream in) throws IOException {
		FSDataOutputStream out = fileSystem.create(path);
		try {
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private String uriToPath(URI uri, String extension) {
		String key = StringUtils.base64encode(uri.toASCIIString());
		StringBuffer sb = new StringBuffer(
				"/opensearchserver/http-download-cache");
		int l = key.length();
		int i = 0;
		while (l > 0) {
			sb.append(Path.SEPARATOR_CHAR);
			if (l > 10)
				sb.append(key.substring(i, i + 10));
			else
				sb.append(key.substring(i));
			l -= 10;
			i += 10;
		}
		if (extension != null) {
			sb.append('.');
			sb.append(extension);
		}
		return sb.toString();
	}

	private final static String META_EXTENSION = "meta";
	private final static String CONTENT_EXTENSION = "content";

	public InputStream storeCache(DownloadItem downloadItem)
			throws IOException, JSONException {
		rwl.r.lock();
		try {
			if (!enabled)
				return downloadItem.getContentInputStream();
			URI uri = downloadItem.getUri();
			Path path = checkPath(uriToPath(uri, META_EXTENSION), true);
			write(path, downloadItem.getMetaAsJson());
			path = checkPath(uriToPath(uri, CONTENT_EXTENSION), true);
			InputStream is = downloadItem.getContentInputStream();
			write(path, is);
			IOUtils.closeQuietly(is);
			System.out.println("STORE CACHE " + uri.toString());
			return fileSystem.open(path);
		} finally {
			rwl.r.unlock();
		}
	}

	public DownloadItem loadCache(URI uri) throws IOException, JSONException,
			URISyntaxException {
		rwl.r.lock();
		try {
			if (!enabled)
				return null;
			Path path = exists(uriToPath(uri, META_EXTENSION));
			if (path == null)
				return null;
			String content = read(path);
			JSONObject json = new JSONObject(content);
			DownloadItem downloadItem = new DownloadItem(uri);
			downloadItem.loadMetaFromJson(json);
			path = exists(uriToPath(uri, CONTENT_EXTENSION));
			downloadItem.setContentInputStream(fileSystem.open(path));
			System.out.println("LOAD CACHE " + uri.toString());
			return downloadItem;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		rwl.r.lock();
		try {
			return enabled;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 * @throws IOException
	 */
	public void setEnabled(boolean enabled) throws IOException {
		rwl.w.lock();
		try {
			this.enabled = enabled;
			save();
		} finally {
			rwl.r.unlock();
		}
	}
}
