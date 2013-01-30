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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class YouTube {

	private final static String API_URL = "http://gdata.youtube.com/feeds/api/videos/";
	private final static String THUMBNAIL = "http://img.youtube.com/vi/";

	public static YouTubeItem getInfo(URL url, HttpDownloader httpDownloader)
			throws MalformedURLException, IOException, URISyntaxException,
			JSONException {
		String videoId = getVideoId(url);
		if (videoId == null)
			throw new IOException("No video ID found: " + url);
		YouTubeItem youtubeItem = YouTubeItemCache.getItem(videoId);
		if (youtubeItem != null) {
			if (Logging.isDebug)
				Logging.debug("YouTube cache");
			return youtubeItem;
		}

		String videoApiURL = API_URL + videoId + "?alt=json";
		String thumbnail = THUMBNAIL + videoId + "/default.jpg";

		DownloadItem downloadItem = httpDownloader.get(new URI(videoApiURL),
				null);
		InputStream inputStream = downloadItem.getContentInputStream();
		if (inputStream == null)
			throw new IOException("No respond returned from YouTube API: "
					+ videoApiURL);
		try {
			youtubeItem = new YouTubeItem(inputStream, videoId, thumbnail);
			YouTubeItemCache.addItem(videoId, youtubeItem);
			return youtubeItem;
		} finally {
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
		}
	}

	private final static Pattern[] idPatterns = {
			Pattern.compile("/embed/([^/]*)"),
			Pattern.compile("/v/([a-zA-Z0-9]*)[&|?]?.*") };

	/*
	 * This method is to extract the Video id from youtube urls like
	 * http://www.youtube.com/watch?v=asdoss-1
	 * http://www.youtube.com/v/Ahg6qcgoay4
	 */
	private static String getVideoId(URL url) throws URISyntaxException {
		URI uri = url.toURI();
		// Checking v=
		List<NameValuePair> pairs = URLEncodedUtils.parse(uri, "UTF-8");
		for (NameValuePair pair : pairs)
			if ("v".equals(pair.getName()))
				return pair.getValue();

		// Checking on path
		String path = uri.getPath();
		for (Pattern pattern : idPatterns) {
			synchronized (pattern) {
				Matcher urlMatcher = pattern.matcher(path);
				if (urlMatcher.matches())
					return urlMatcher.group(1);
			}
		}
		return null;
	}

	public final static void main(String[] args) throws MalformedURLException,
			IOException, URISyntaxException, JSONException {

		HttpDownloader downloader = new HttpDownloader("OpenSearchServer",
				false, null);
		String[] urls = {
				"http://www.youtube.com/watch?h=test&v=O04CHuJaPWc",
				"http://www.youtube.com/watch?v=HmQk3ovfiF0&feature=g-all-f",
				"http://www.youtube.com/v/HmQk3ovfiF0&feature=g-all-f",
				"http://www.youtube.com/v/Ig1WxMI9bxQ&hl=fr&fs=1&color1=0x2b405b&color2=0x6b8ab6",
				"http://www.youtube.com/v/Ig1WxMI9bxQ?hl=fr" };
		for (String u : urls) {
			URL url = new URL(u);
			System.out.println(getInfo(url, downloader).toJson(url));
		}
		downloader.release();

	}
}
