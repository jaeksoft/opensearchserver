/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.cache;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.webservice.document.DocumentUpdate;
import com.qwazr.utils.json.JsonMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocalFileCrawlCache extends CrawlCacheProvider {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String rootPath = null;

	public LocalFileCrawlCache() {
		super(CrawlCacheProviderEnum.LOCAL_FILE);
	}

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
			if (!f.exists()) {
				ClientFactory.INSTANCE.properties.checkChroot(f);
				f.mkdirs();
			}
			if (!f.exists())
				throw new IOException("The folder " + f.getAbsolutePath() + " does not exists");
			if (!f.isDirectory())
				throw new IOException("The folder " + f.getAbsolutePath() + " does not exists");
			rootPath = f.getAbsolutePath();
		} finally {
			rwl.w.unlock();
		}
	}

	private final static String PATH_HTTP_DOWNLOAD_CACHE = File.separator + "http-download-cache";

	private final static String META_EXTENSION = ".meta";

	private final static String META_EXTENSION_COMPRESSED = ".meta.gz";

	private final static String CONTENT_EXTENSION = ".content";

	private final static String CONTENT_EXTENSION_COMPRESSED = ".content.gz";

	private final static String INDEXED_EXTENSION_COMPRESSED = ".indexed.gz";

	private final static String[] EXTENSIONS =
			new String[] { META_EXTENSION, META_EXTENSION_COMPRESSED, CONTENT_EXTENSION, CONTENT_EXTENSION_COMPRESSED };

	private File checkPath(String filePrefix, String fileExtension) throws IOException {
		final File file = new File(filePrefix + fileExtension);
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
		}
		return file;
	}

	private long purge(File[] files, long expiration) throws IOException {
		if (files == null)
			return 0;
		long count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				count += purge(file.listFiles(), expiration);
				File[] fs = file.listFiles();
				if (fs == null)
					continue;
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
			File file = new File(rootPath + File.separator + PATH_HTTP_DOWNLOAD_CACHE);
			return purge(file.listFiles(), expiration);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String getConfigurationInformation() {
		return "Please provide the path of the cache directory (Eg.: /var/local/oss_crawl_cache)";
	}

	@Override
	public Item getItem(URI uri, long expirationTime) throws UnsupportedEncodingException {
		return new LocalFileItem(uri, expirationTime);
	}

	public class LocalFileItem extends Item {

		private final URI uri;
		private final String filePrefix;
		private final long expirationTime;

		private LocalFileItem(final URI uri, long expirationTime) throws UnsupportedEncodingException {
			this.uri = uri;
			this.filePrefix = uriToPath(uri, rootPath + File.separator + PATH_HTTP_DOWNLOAD_CACHE, 10, File.separator,
					32);
			this.expirationTime = expirationTime;
		}

		@Override
		public InputStream store(DownloadItem downloadItem) throws IOException, JSONException {
			rwl.r.lock();
			try {
				final URI uri = downloadItem.getUri();
				if (!uri.equals(this.uri))
					throw new IOException("The URI does not match: " + uri + " / " + this.uri);
				final File metaFile = checkPath(filePrefix, META_EXTENSION_COMPRESSED);
				FileUtils.writeStringToGzipFile(downloadItem.getMetaAsJson(), StandardCharsets.UTF_8, metaFile);
				final File contentFile = checkPath(filePrefix, CONTENT_EXTENSION_COMPRESSED);
				try (final InputStream is = downloadItem.getContentInputStream()) {
					FileUtils.writeToGzipFile(is, contentFile);
				}
				return FileUtils.readFromGzipFile(contentFile);
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public DownloadItem load() throws IOException, JSONException, URISyntaxException {
			rwl.r.lock();
			try {
				final File metaFile = checkPath(filePrefix, META_EXTENSION_COMPRESSED);
				if (!metaFile.exists())
					return null;
				if (expirationTime != 0)
					if (metaFile.lastModified() < expirationTime)
						return null;
				final String content = FileUtils.readStringFromGzipFile(StandardCharsets.UTF_8, metaFile);
				final JSONObject json = new JSONObject(content);
				final DownloadItem downloadItem = new DownloadItem(uri, json);
				final File contentFile = checkPath(filePrefix, CONTENT_EXTENSION_COMPRESSED);
				downloadItem.setContentInputStream(FileUtils.readFromGzipFile(contentFile));
				return downloadItem;
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public boolean flush() throws IOException {
			rwl.r.lock();
			try {
				boolean deleted = false;
				for (String fileExtension : EXTENSIONS) {
					final File file = checkPath(filePrefix, fileExtension);
					if (file.exists())
						deleted = file.delete() || deleted;
				}
				return deleted;
			} finally {
				rwl.r.unlock();
			}
		}

		@Override
		public void store(List<ParserResultItem> parserResults) throws IOException {
			rwl.r.lock();
			try {
				if (parserResults == null || parserResults.isEmpty())
					return;
				final List<DocumentUpdate> documentUpdates = new ArrayList<>();
				parserResults.forEach(parserResultItem -> documentUpdates.add(
						new DocumentUpdate(parserResultItem.getParserDocument())));
				final File file = checkPath(filePrefix, INDEXED_EXTENSION_COMPRESSED);
				JsonMapper.MAPPER.writeValue(FileUtils.writeToGzipFile(file), documentUpdates);
			} finally {
				rwl.r.unlock();
			}
		}
	}
}
