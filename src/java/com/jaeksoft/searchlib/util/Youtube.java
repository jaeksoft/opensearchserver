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
package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;

public class Youtube {

	private final static String API_URL = "http://gdata.youtube.com/feeds/api/videos/";

	private final static int TIMEOUT = 2000;

	private final static Pattern urlPattern = Pattern
			.compile("http.*\\?v=([a-zA-Z0-9_\\-]+)(?:&.)*");

	public static YoutubeItem getInfo(URL url) throws MalformedURLException,
			IOException, ServiceException {
		String videoId = getVideoId(url);
		YoutubeItem youtubeItem = YoutubeItemCache.getItem(videoId);
		if (youtubeItem != null)
			return youtubeItem;
		YouTubeService youTubeService = new YouTubeService(null);
		youTubeService.setConnectTimeout(TIMEOUT);
		String videoApiURL = API_URL + getVideoId(url);
		VideoEntry videoEntry = youTubeService.getEntry(new URL(videoApiURL),
				VideoEntry.class);
		youtubeItem = new YoutubeItem(videoEntry.getMediaGroup());
		YoutubeItemCache.addItem(videoId, youtubeItem);
		return youtubeItem;
	}

	/*
	 * This method is to extract the Video id from youtube urls like
	 * http://www.youtube.com/watch?v=asdoss-1
	 */
	private static String getVideoId(URL url) {
		synchronized (urlPattern) {
			Matcher urlMatcher = urlPattern.matcher(url.toExternalForm());
			if (urlMatcher.matches())
				return urlMatcher.group(1);
		}
		return null;
	}

	public final static void main(String[] args) throws MalformedURLException,
			IOException, ServiceException {
		System.out.println(getInfo(new URL(
				"http://www.youtube.com/watch?v=O04CHuJaPWc")));
	}

}
