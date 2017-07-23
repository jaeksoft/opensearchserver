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

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.util.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class CrawlCacheProvider {

	private final CrawlCacheProviderEnum cacheType;

	public CrawlCacheProvider(CrawlCacheProviderEnum cacheType) {
		this.cacheType = cacheType;
	}

	final public CrawlCacheProviderEnum getCacheType() {
		return cacheType;
	}

	public abstract String getInfos() throws IOException;

	public abstract void init(String configString) throws IOException;

	public abstract void close();

	public abstract String getConfigurationInformation();

	public abstract Item getItem(URI uri, long expirationTime) throws UnsupportedEncodingException;

	public abstract long flush(long expirationTime) throws IOException;

	final String uriToPath(URI uri, String rootPath, int hashDepth, String separatorChar, int splitSize)
			throws UnsupportedEncodingException {
		final String key = StringUtils.base64encode(uri.toASCIIString());
		final StringBuilder sb = new StringBuilder(rootPath);
		int l = key.length();
		if (l > hashDepth)
			l = hashDepth;
		while (l > 0) {
			sb.append(separatorChar);
			sb.append(key.charAt(l--));
		}
		l = key.length();
		int i = 0;
		while (l > 0) {
			sb.append(separatorChar);
			if (l > splitSize)
				sb.append(key.substring(i, i + splitSize));
			else
				sb.append(key.substring(i));
			l -= splitSize;
			i += splitSize;
		}
		return sb.toString();
	}

	public abstract static class Item {

		public abstract InputStream store(DownloadItem downloadItem) throws IOException, JSONException;

		public abstract DownloadItem load() throws IOException, JSONException, URISyntaxException;

		public abstract boolean flush() throws IOException;

		public abstract void store(List<ParserResultItem> parserResults) throws IOException;
	}
}
