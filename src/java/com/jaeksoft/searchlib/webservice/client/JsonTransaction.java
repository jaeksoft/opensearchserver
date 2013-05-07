/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;

public class JsonTransaction {

	private final StringBuffer sb;

	private boolean firstParam;

	public JsonTransaction(RestJsonClient restJsonClient, String path,
			String indexName) throws UnsupportedEncodingException {
		firstParam = true;
		sb = new StringBuffer(restJsonClient.oss_url);
		if (indexName != null)
			path = path
					.replace("{index}", LinkUtils.UTF8_URL_Encode(indexName));
		sb.append(path);
		if (restJsonClient.oss_login != null)
			addParam("login", restJsonClient.oss_login);
		if (restJsonClient.oss_key != null)
			addParam("key", restJsonClient.oss_key);

	}

	public void addParam(String name, String value)
			throws UnsupportedEncodingException {
		if (firstParam) {
			sb.append('?');
			firstParam = false;
		} else
			sb.append('&');
		sb.append(name);
		sb.append('=');
		sb.append(LinkUtils.UTF8_URL_Encode(value));
	}

	public URI getURI() throws URISyntaxException {
		return new URI(sb.toString());
	}

	public JSONObject get(HttpDownloader downloader)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, JSONException {
		DownloadItem dlItem = downloader.get(getURI(), null);
		return checkJsonResult(dlItem);
	}

	public JSONObject put(HttpDownloader downloader)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, JSONException {
		DownloadItem dlItem = downloader.put(getURI(), null, null, null);
		return checkJsonResult(dlItem);
	}

	public JSONObject post(HttpDownloader downloader)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, JSONException {
		DownloadItem dlItem = downloader.post(getURI(), null, null, null);
		return checkJsonResult(dlItem);
	}

	public JSONObject delete(HttpDownloader downloader)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, JSONException {
		DownloadItem dlItem = downloader.delete(getURI(), null, null);
		return checkJsonResult(dlItem);
	}

	private JSONObject checkJsonResult(DownloadItem downloadItem)
			throws JSONException, IOException, SearchLibException {
		downloadItem.checkNoError(200);
		JSONObject json = new JSONObject(downloadItem.getContentAsString());
		JSONObject jsonResult = json.getJSONObject("result");
		if (!"true".equals(jsonResult.getString("@successful")))
			throw new JSONException("Wrong server answer");
		return json;
	}
}
