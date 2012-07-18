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
import com.jaeksoft.searchlib.Logging;

public class Youtube {
	private YouTubeService youTubeService = null;
	private static String API_URL = "http://gdata.youtube.com/feeds/api/videos/";
	private static int TIMEOUT = 2000;
	private YoutubeItem youtubeItem = null;

	public YoutubeItem getInfo(URL url) {
		try {
			youTubeService = new YouTubeService(null);
			youTubeService.setConnectTimeout(TIMEOUT);
			String videoApiURL = API_URL + getVideoId(url);
			VideoEntry videoEntry = youTubeService.getEntry(
					new URL(videoApiURL), VideoEntry.class);
			youtubeItem = new YoutubeItem(videoEntry.getMediaGroup());
		} catch (MalformedURLException e) {
			Logging.error(e);
		} catch (IOException e) {
			Logging.error(e);
		} catch (ServiceException e) {
			Logging.error(e);
		}
		return youtubeItem;

	}

	/*
	 * This method is to extract the Video id from youtube urls like
	 * http://www.youtube.com/watch?v=asdoss-1 http://www.youtube.com/v/asdoss-1
	 */
	private String getVideoId(URL url) {
		String videoID = null;
		Pattern urlPattern = Pattern
				.compile("http.*\\?v=([a-zA-Z0-9_\\-]+)(?:&.)*");
		Matcher urlMatcher = urlPattern.matcher(url.toExternalForm());
		if (urlMatcher.matches()) {
			videoID = urlMatcher.group(1);
		}
		return videoID;
	}

}
