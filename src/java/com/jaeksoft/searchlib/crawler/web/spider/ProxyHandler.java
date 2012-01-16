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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;

public class ProxyHandler {

	private HttpHost proxy = null;

	private Set<String> exclusionSet = null;

	public ProxyHandler(WebPropertyManager webPropertyManager)
			throws SearchLibException {
		if (!webPropertyManager.getProxyEnabled().getValue())
			return;
		String proxyHost = webPropertyManager.getProxyHost().getValue();
		int proxyPort = webPropertyManager.getProxyPort().getValue();
		if (proxyHost == null || proxyHost.trim().length() == 0
				|| proxyPort == 0)
			return;
		proxy = new HttpHost(proxyHost, proxyPort);
		exclusionSet = new TreeSet<String>();
		BufferedReader br = new BufferedReader(new StringReader(
				webPropertyManager.getProxyExclusion().getValue()));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					exclusionSet.add(line);
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.closeQuietly(br);
		}
	}

	public void check(HttpClient httpClient, URI uri) {
		if (proxy == null || uri == null)
			return;
		if (exclusionSet.contains(uri.getHost()))
			return;
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);
	}

}
