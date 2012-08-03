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

import java.net.URL;

import org.json.simple.JSONObject;

import com.google.gdata.data.youtube.YouTubeMediaGroup;

public class YouTubeItem {

	private final String title;
	private final String description;
	private final String videoId;
	private final String thumbnail;

	public YouTubeItem(YouTubeMediaGroup youTubeMediaGroup, String videoId,
			String thumbnail) {
		this.title = youTubeMediaGroup.getTitle().getPlainTextContent();
		this.description = youTubeMediaGroup.getDescription()
				.getPlainTextContent();
		this.videoId = videoId;
		this.thumbnail = thumbnail;
	}

	final public String getTitle() {
		return title;
	}

	final public String getDescription() {
		return description;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	@Override
	final public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Title: ");
		sb.append(title);
		sb.append(" - Description: ");
		sb.append(description);
		sb.append(" - VideoId: ");
		sb.append(videoId);
		sb.append(" - thumbnail: ");
		sb.append(thumbnail);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	final public String toJson(URL url) {
		JSONObject json = new JSONObject();
		json.put("url", url.toExternalForm());
		json.put("title", title);
		json.put("description", description);
		json.put("videoId", videoId);
		json.put("thumbnail", thumbnail);
		return json.toJSONString();
	}
}
