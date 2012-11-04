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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;
import com.jaeksoft.searchlib.Logging;

public class YouTube {

	private final static String API_URL = "http://gdata.youtube.com/feeds/api/videos/";
	private final static String THUMBNAIL = "http://img.youtube.com/vi/";

	private final static int TIMEOUT = 2000;

	public static YouTubeItem getInfo(URL url) throws MalformedURLException,
			IOException, ServiceException, URISyntaxException {
		String videoId = getVideoId(url);
		if (videoId == null)
			throw new IOException("No video ID found: " + url);
		YouTubeItem youtubeItem = YouTubeItemCache.getItem(videoId);
		if (youtubeItem != null) {
			if (Logging.isDebug)
				Logging.debug("YouTube cache");
			return youtubeItem;
		}
		YouTubeService youTubeService = new YouTubeService(null);
		youTubeService.setConnectTimeout(TIMEOUT);
		String videoApiURL = API_URL + videoId;
		String thumbnail = THUMBNAIL + videoId + "/default.jpg";
		VideoEntry videoEntry = youTubeService.getEntry(new URL(videoApiURL),
				VideoEntry.class);
		youtubeItem = new YouTubeItem(videoEntry.getMediaGroup(), videoId,
				thumbnail);
		YouTubeItemCache.addItem(videoId, youtubeItem);
		return youtubeItem;
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
			IOException, ServiceException, URISyntaxException {

		String[] urls = {
				"http://www.youtube.com/watch?h=test&v=O04CHuJaPWc",
				"http://www.youtube.com/watch?v=HmQk3ovfiF0&feature=g-all-f",
				"http://www.youtube.com/v/HmQk3ovfiF0&feature=g-all-f",
				"http://www.youtube.com/v/Ig1WxMI9bxQ&hl=fr&fs=1&color1=0x2b405b&color2=0x6b8ab6",
				"http://www.youtube.com/v/Ig1WxMI9bxQ?hl=fr" };
		for (String u : urls) {
			URL url = new URL(u);
			String yti = getVideoId(url);
			System.out.println(yti);
		}

	}
}
