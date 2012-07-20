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
package com.jaeksoft.searchlib.util.video;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.google.gdata.util.ServiceException;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class Vimeo {

	private final static String API_URL = "http://vimeo.com/api/v2/video/";

	private final static Pattern urlPattern = Pattern
			.compile("http://vimeo.com/([0-9]+)");

	public static VimeoItem getInfo(URL url, HttpDownloader httpDownloader)
			throws MalformedURLException, IOException, ServiceException,
			URISyntaxException, JSONException {
		String videoId = getVideoId(url);
		if (videoId == null)
			throw new IOException("No video ID found: " + url);
		VimeoItem vimeoItem = VimeoItemCache.getItem(videoId);
		if (vimeoItem != null) {
			if (Logging.isDebug)
				Logging.debug("Dailymotion cache");
			return vimeoItem;
		}
		String videoApiURL = API_URL + videoId;

		InputStream vimeoResponse = getVimeonResponse(httpDownloader,
				videoApiURL);
		if (vimeoResponse == null)
			throw new IOException("No respond returned from Dailymotion API: "
					+ videoApiURL);
		vimeoItem = new VimeoItem(vimeoResponse);
		VimeoItemCache.addItem(videoId, vimeoItem);
		return vimeoItem;
	}

	/*
	 * This method is to extract the Video id from Dailymotion url
	 * http://www.dailymotion.com/video/xjlmik
	 */
	private static String getVideoId(URL url) {
		synchronized (urlPattern) {
			Matcher urlMatcher = urlPattern.matcher(url.toExternalForm());
			if (urlMatcher.matches())
				return urlMatcher.group(1);
		}
		return null;
	}

	private static InputStream getVimeonResponse(HttpDownloader httpDownload,
			String url) throws ClientProtocolException, IOException,
			URISyntaxException {
		DownloadItem downloadItem = httpDownload.get(new URI(url), null);
		return downloadItem.getContentInputStream();
	}

	public final static void main(String[] args) throws MalformedURLException,
			IOException, ServiceException, URISyntaxException, JSONException {
		String url = "http://vimeo.com/45339397";
		String videoID = getVideoId(new URL(url));
		System.out.println(videoID);
	}
}
