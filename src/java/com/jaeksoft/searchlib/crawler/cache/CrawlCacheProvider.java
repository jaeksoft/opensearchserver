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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.StringUtils;

public abstract class CrawlCacheProvider {

	public abstract String getInfos() throws IOException;

	public abstract void init(String configString) throws IOException;

	public abstract void close();

	public abstract String getConfigurationInformation();

	public abstract InputStream store(DownloadItem downloadItem)
			throws IOException, JSONException;

	public abstract DownloadItem load(URI uri, long expirationTime)
			throws IOException, JSONException, URISyntaxException;

	public abstract long flush(long expirationTime) throws IOException;

	final protected String uriToPath(URI uri, String rootPath, int hashDepth,
			String separatorChar, String extension, int splitSize)
			throws UnsupportedEncodingException {
		String key = StringUtils.base64encode(uri.toASCIIString());
		StringBuffer sb = new StringBuffer(rootPath);
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
		if (extension != null) {
			sb.append('.');
			sb.append(extension);
		}
		return sb.toString();
	}
}
