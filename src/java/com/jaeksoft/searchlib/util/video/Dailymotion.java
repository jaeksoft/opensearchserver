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

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class Dailymotion {

	private final static String API_URL = "https://api.dailymotion.com/video/";
	private final static String THUMBNAIL = "http://www.dailymotion.com/thumbnail/video/";
	private final static Pattern[] idPatterns = {
			Pattern.compile(".*/video/([^_&]+).*"),
			Pattern.compile("/swf/([^&]+).*") };

	public static DailymotionItem getInfo(URL url, HttpDownloader httpDownloader)
			throws MalformedURLException, IOException, URISyntaxException,
			JSONException, IllegalStateException, SearchLibException {
		String videoId = getVideoId(url);
		if (videoId == null)
			throw new IOException("No video ID found: " + url);
		DailymotionItem dailymotionItem = DailymotionItemCache.getItem(videoId);
		if (dailymotionItem != null) {
			if (Logging.isDebug)
				Logging.debug("Dailymotion cache");
			return dailymotionItem;
		}
		String videoApiURL = API_URL + videoId;
		String thumbnail = THUMBNAIL + videoId;
		DownloadItem downloadItem = httpDownloader.get(new URI(videoApiURL),
				null);
		InputStream dailymotionResponse = downloadItem.getContentInputStream();
		if (dailymotionResponse == null)
			throw new IOException("No respond returned from Dailymotion API: "
					+ videoApiURL);
		try {
			dailymotionItem = new DailymotionItem(dailymotionResponse, videoId,
					thumbnail);
			DailymotionItemCache.addItem(videoId, dailymotionItem);
			return dailymotionItem;
		} finally {
			if (dailymotionResponse != null)
				IOUtils.closeQuietly(dailymotionResponse);
		}
	}

	/*
	 * This method is to extract the Video id from Dailymotion url
	 * http://www.dailymotion.com/video/xjlmik
	 */
	private static String getVideoId(URL url) throws URISyntaxException {
		URI uri = url.toURI();
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
			IOException, URISyntaxException, JSONException,
			IllegalStateException, SearchLibException {
		HttpDownloader downloader = new HttpDownloader("OpenSearchServer",
				false, null);
		String[] urls = {
				"http://www.dailymotion.com/video/xjlmik_raphael-perez-emmanuel-keller-open-search-server_tech?no_track=1",
				"http://www.dailymotion.com/swf/x4f4ty&v3=1&related=0",
				"http://www.dailymotion.com/embed/video/xayzui",
				"http://www.dailymotion.com/embed/video/xl4c8y?logo=0&autoPlay=1&repeat=1&forcedQuality=sd" };
		for (String u : urls) {
			URL url = new URL(u);
			System.out.println(getInfo(url, downloader).toJson(url));
		}
		downloader.release();

	}
}
