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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;

import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class LocalFileCrawlCache extends CrawlCacheProvider {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private File rootPath = null;

	@Override
	public void close() {
		rootPath = null;
	}

	@Override
	public String getInfos() throws IOException {
		return rootPath != null ? rootPath.getAbsolutePath() : null;
	}

	@Override
	public void init(String configString) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public InputStream store(DownloadItem downloadItem) throws IOException,
			JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DownloadItem load(URI uri) throws IOException, JSONException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getConfigurationInformation() {
		return "Please provide the path of the cache directory (Eg.: /var/local/oss_crawl_cache)";
	}
}
